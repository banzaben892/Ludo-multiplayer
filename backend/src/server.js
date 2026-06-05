require('dotenv').config();
const express     = require('express');
const mongoose    = require('mongoose');
const cors        = require('cors');
const helmet      = require('helmet');
const morgan      = require('morgan');
const rateLimit   = require('express-rate-limit');
const { Server }  = require('socket.io');
const http        = require('http');

const authRouter        = require('./routes/auth');
const paymentRouter     = require('./routes/payment');
const competitionRouter = require('./routes/competition');
const adminRouter       = require('./routes/admin');

const app    = express();
const server = http.createServer(app);
const io     = new Server(server, { cors: { origin: '*' } });

// ── Middlewares ───────────────────────────────────────────────
app.use(helmet());
app.use(cors({ origin: process.env.ALLOWED_ORIGINS?.split(',') || '*' }));
app.use(morgan('dev'));
app.use(express.json({ limit: '10kb' }));
app.use(express.urlencoded({ extended: true }));

// Rate limiting global
app.use('/api/', rateLimit({
  windowMs: 15 * 60 * 1000,
  max:      100,
  message:  { message: 'Trop de requêtes, réessayez dans 15 min.' },
}));
// Rate limiting strict sur auth
app.use('/api/auth/', rateLimit({
  windowMs: 15 * 60 * 1000,
  max:      10,
  message:  { message: 'Trop de tentatives de connexion.' },
}));

// ── Routes API ────────────────────────────────────────────────
app.use('/api/auth',         authRouter);
app.use('/api/payment',      paymentRouter);
app.use('/api/competitions', competitionRouter);
app.use('/api/admin',        adminRouter);

// Health check
app.get('/health', (_, res) =>
  res.json({ status: 'ok', env: process.env.NODE_ENV, ts: new Date() })
);

// 404
app.use((req, res) => res.status(404).json({ message: 'Route non trouvée' }));

// Gestion erreurs globale
app.use((err, req, res, _next) => {
  console.error(err.stack);
  const status = err.status || 500;
  res.status(status).json({
    message: process.env.NODE_ENV === 'production'
      ? 'Erreur interne'
      : err.message,
  });
});

// ── Socket.IO — Matchmaking & Jeu temps-réel ─────────────────
const rooms = new Map(); // roomId → { players, state, competition }

io.on('connection', (socket) => {
  console.log(`🔌 Connexion: ${socket.id}`);

  // Rejoindre une salle de compétition
  socket.on('join_room', ({ competitionId, userId, username, color }) => {
    socket.join(competitionId);
    if (!rooms.has(competitionId)) rooms.set(competitionId, { players: [], ready: 0 });
    const room = rooms.get(competitionId);
    const existing = room.players.find(p => p.userId === userId);
    if (!existing) room.players.push({ socketId: socket.id, userId, username, color });
    io.to(competitionId).emit('room_update', { players: room.players });
    console.log(`🎮 ${username} a rejoint la salle ${competitionId}`);
  });

  // Joueur prêt
  socket.on('player_ready', ({ competitionId }) => {
    const room = rooms.get(competitionId);
    if (!room) return;
    room.ready = (room.ready || 0) + 1;
    if (room.ready >= room.players.length && room.players.length >= 2) {
      io.to(competitionId).emit('game_start', { players: room.players });
      room.ready = 0;
    }
  });

  // Résultat d'un lancer de dé (synchronisation multi-joueurs)
  socket.on('dice_roll', ({ competitionId, userId, dice }) => {
    socket.to(competitionId).emit('opponent_dice', { userId, dice });
  });

  // Mouvement de pion
  socket.on('piece_move', ({ competitionId, userId, pieceId, finalPos }) => {
    socket.to(competitionId).emit('opponent_move', { userId, pieceId, finalPos });
  });

  // Résultat final
  socket.on('game_over', ({ competitionId, ranking, totalTurns }) => {
    io.to(competitionId).emit('game_result', { ranking, totalTurns });
    rooms.delete(competitionId);
  });

  // Message chat in-game
  socket.on('chat_msg', ({ competitionId, username, text }) => {
    io.to(competitionId).emit('chat_msg', { username, text, ts: Date.now() });
  });

  socket.on('disconnect', () => {
    console.log(`🔌 Déconnexion: ${socket.id}`);
    rooms.forEach((room, compId) => {
      room.players = room.players.filter(p => p.socketId !== socket.id);
      io.to(compId).emit('room_update', { players: room.players });
    });
  });
});

// ── Démarrage ─────────────────────────────────────────────────
const PORT = process.env.PORT || 3000;
mongoose.connect(process.env.MONGO_URI)
  .then(() => {
    console.log('✅ MongoDB connecté');
    server.listen(PORT, () =>
      console.log(`🚀 Serveur Ludo Master Pro — port ${PORT}`)
    );
  })
  .catch(err => { console.error('❌ MongoDB:', err); process.exit(1); });

module.exports = { app, io };
