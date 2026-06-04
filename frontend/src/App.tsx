import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import DarkModeIcon from '@mui/icons-material/DarkMode';
import ErrorOutlineIcon from '@mui/icons-material/ErrorOutline';
import FitnessCenterIcon from '@mui/icons-material/FitnessCenter';
import LocalDiningIcon from '@mui/icons-material/LocalDining';
import RefreshIcon from '@mui/icons-material/Refresh';
import SelfImprovementIcon from '@mui/icons-material/SelfImprovement';
import TaskAltIcon from '@mui/icons-material/TaskAlt';
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  CircularProgress,
  Container,
  CssBaseline,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Grid,
  IconButton,
  LinearProgress,
  Snackbar,
  Stack,
  ThemeProvider,
  Tooltip,
  Typography,
  createTheme,
} from '@mui/material';
import { useCallback, useEffect, useMemo, useState } from 'react';
import {
  USUARIO_DEMO_ID,
  completarHabito,
  obtenerAvatar,
  obtenerHabitos,
  pasarDia,
} from './services/vidificationApi';
import type { Avatar, EstadoAvatar, Habito } from './types/vidification';

const theme = createTheme({
  palette: {
    mode: 'light',
    primary: {
      main: '#246b5b',
    },
    secondary: {
      main: '#7c4d25',
    },
    background: {
      default: '#f6f7f2',
      paper: '#ffffff',
    },
  },
  shape: {
    borderRadius: 8,
  },
  typography: {
    fontFamily:
      'Inter, ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif',
    h1: {
      fontSize: '2rem',
      fontWeight: 800,
    },
    h2: {
      fontSize: '1.2rem',
      fontWeight: 800,
    },
    button: {
      fontWeight: 700,
      textTransform: 'none',
    },
  },
});

const estadoColor: Record<string, 'success' | 'info' | 'warning' | 'error'> = {
  SALUDABLE: 'success',
  NORMAL: 'info',
  DECAIDO: 'warning',
  CRITICO: 'error',
};

const estadoGradiente: Record<string, string> = {
  SALUDABLE: 'linear-gradient(135deg, #d7f7df 0%, #f7ffe6 100%)',
  NORMAL: 'linear-gradient(135deg, #dceeff 0%, #f4fbff 100%)',
  DECAIDO: 'linear-gradient(135deg, #fff1d7 0%, #fffaf0 100%)',
  CRITICO: 'linear-gradient(135deg, #ffe0dc 0%, #fff5f3 100%)',
};

function iconoCategoria(categoria: string) {
  if (categoria === 'ALIMENTACION') return <LocalDiningIcon />;
  if (categoria === 'EJERCICIO') return <FitnessCenterIcon />;
  if (categoria === 'DESCANSO') return <DarkModeIcon />;
  return <SelfImprovementIcon />;
}

function colorPorEstado(estado: EstadoAvatar) {
  return estadoColor[estado] ?? 'info';
}

function App() {
  const [avatar, setAvatar] = useState<Avatar | null>(null);
  const [habitos, setHabitos] = useState<Habito[]>([]);
  const [cargando, setCargando] = useState(true);
  const [accionandoId, setAccionandoId] = useState<number | null>(null);
  const [pasandoDia, setPasandoDia] = useState(false);
  const [dialogoDiaAbierto, setDialogoDiaAbierto] = useState(false);
  const [mensaje, setMensaje] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const cargarDatos = useCallback(async () => {
    setError(null);
    const [avatarActual, habitosActuales] = await Promise.all([
      obtenerAvatar(USUARIO_DEMO_ID),
      obtenerHabitos(USUARIO_DEMO_ID),
    ]);
    setAvatar(avatarActual);
    setHabitos(habitosActuales);
  }, []);

  useEffect(() => {
    cargarDatos()
      .catch(() => setError('No se pudo conectar con la API de Vidification.'))
      .finally(() => setCargando(false));
  }, [cargarDatos]);

  const completadosHoy = useMemo(
    () => habitos.filter((habito) => habito.completadoHoy).length,
    [habitos]
  );

  async function manejarCompletar(habito: Habito) {
    setAccionandoId(habito.id);
    setError(null);
    try {
      const resultado = await completarHabito(habito.id);
      setMensaje(`Completaste ${resultado.habito}: +${resultado.puntosGanados} pts`);
      await cargarDatos();
    } catch {
      setError('No se pudo completar el hábito.');
    } finally {
      setAccionandoId(null);
    }
  }

  async function manejarPasarDia() {
    setPasandoDia(true);
    setError(null);
    try {
      await pasarDia(USUARIO_DEMO_ID);
      setMensaje('Día cerrado. Los hábitos pendientes afectaron la vitalidad.');
      setDialogoDiaAbierto(false);
      await cargarDatos();
    } catch {
      setError('No se pudo cerrar el día.');
    } finally {
      setPasandoDia(false);
    }
  }

  const estado = avatar?.estado ?? 'NORMAL';
  const estadoMui = colorPorEstado(estado);

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Box sx={{ minHeight: '100vh', bgcolor: 'background.default', py: { xs: 2, md: 4 } }}>
        <Container maxWidth="lg">
          <Stack spacing={3}>
            <Stack
              direction={{ xs: 'column', sm: 'row' }}
              alignItems={{ xs: 'flex-start', sm: 'center' }}
              justifyContent="space-between"
              gap={2}
            >
              <Box>
                <Typography component="h1" variant="h1">
                  Vidification
                </Typography>
                <Typography color="text.secondary">
                  {avatar ? avatar.nombreUsuario : 'Jugador'} · {completadosHoy}/{habitos.length}{' '}
                  hábitos completos
                </Typography>
              </Box>
              <Stack direction="row" spacing={1}>
                <Tooltip title="Actualizar">
                  <IconButton
                    aria-label="Actualizar"
                    onClick={() => {
                      setCargando(true);
                      cargarDatos()
                        .catch(() => setError('No se pudieron actualizar los datos.'))
                        .finally(() => setCargando(false));
                    }}
                  >
                    <RefreshIcon />
                  </IconButton>
                </Tooltip>
                <Button
                  color="secondary"
                  startIcon={<DarkModeIcon />}
                  variant="contained"
                  onClick={() => setDialogoDiaAbierto(true)}
                  disabled={cargando || pasandoDia}
                >
                  Terminar día
                </Button>
              </Stack>
            </Stack>

            {error ? (
              <Alert severity="error" icon={<ErrorOutlineIcon />}>
                {error}
              </Alert>
            ) : null}

            <Grid container spacing={3}>
              <Grid item xs={12} md={5}>
                <Card
                  elevation={0}
                  sx={{
                    height: '100%',
                    border: '1px solid',
                    borderColor: 'divider',
                    background: estadoGradiente[estado] ?? estadoGradiente.NORMAL,
                  }}
                >
                  <CardContent>
                    {cargando && !avatar ? (
                      <Stack alignItems="center" justifyContent="center" sx={{ minHeight: 320 }}>
                        <CircularProgress />
                      </Stack>
                    ) : avatar ? (
                      <Stack spacing={3}>
                        <Stack direction="row" alignItems="center" justifyContent="space-between">
                          <Chip label={avatar.estado} color={estadoMui} />
                          <Typography fontWeight={800}>{avatar.puntos} pts</Typography>
                        </Stack>
                        <Stack alignItems="center" spacing={2}>
                          <Box
                            sx={{
                              width: 156,
                              height: 156,
                              borderRadius: '50%',
                              bgcolor: 'rgba(255,255,255,0.76)',
                              display: 'grid',
                              placeItems: 'center',
                              boxShadow: 'inset 0 0 0 1px rgba(0,0,0,0.06)',
                              transition: 'transform 180ms ease',
                              '&:hover': {
                                transform: 'scale(1.03)',
                              },
                            }}
                          >
                            <Typography aria-label={avatar.estado} sx={{ fontSize: 76 }}>
                              {avatar.emoji}
                            </Typography>
                          </Box>
                          <Box sx={{ width: '100%' }}>
                            <Stack direction="row" justifyContent="space-between" sx={{ mb: 1 }}>
                              <Typography fontWeight={700}>Vitalidad</Typography>
                              <Typography color="text.secondary">{avatar.vitalidad}/100</Typography>
                            </Stack>
                            <LinearProgress
                              variant="determinate"
                              value={avatar.vitalidad}
                              color={estadoMui}
                              sx={{ height: 12, borderRadius: 8 }}
                            />
                          </Box>
                          <Typography align="center" color="text.secondary">
                            {avatar.mensaje}
                          </Typography>
                        </Stack>
                      </Stack>
                    ) : (
                      <Stack
                        alignItems="center"
                        justifyContent="center"
                        spacing={2}
                        sx={{ minHeight: 320 }}
                      >
                        <Box
                          sx={{
                            width: 132,
                            height: 132,
                            borderRadius: '50%',
                            bgcolor: 'rgba(255,255,255,0.76)',
                            display: 'grid',
                            placeItems: 'center',
                            boxShadow: 'inset 0 0 0 1px rgba(0,0,0,0.06)',
                          }}
                        >
                          <Typography sx={{ fontSize: 64 }}>🙂</Typography>
                        </Box>
                        <Typography fontWeight={800}>Avatar sin cargar</Typography>
                        <Typography align="center" color="text.secondary">
                          La pantalla está lista; falta levantar la API en el puerto 8080.
                        </Typography>
                      </Stack>
                    )}
                  </CardContent>
                </Card>
              </Grid>

              <Grid item xs={12} md={7}>
                <Stack spacing={2}>
                  <Typography component="h2" variant="h2">
                    Hábitos de hoy
                  </Typography>
                  {cargando && habitos.length === 0 ? (
                    <Card elevation={0} sx={{ border: '1px solid', borderColor: 'divider' }}>
                      <CardContent>
                        <Stack alignItems="center" sx={{ py: 5 }}>
                          <CircularProgress />
                        </Stack>
                      </CardContent>
                    </Card>
                  ) : (
                    habitos.map((habito) => (
                      <Card
                        key={habito.id}
                        elevation={0}
                        sx={{
                          border: '1px solid',
                          borderColor: habito.completadoHoy ? 'success.light' : 'divider',
                        }}
                      >
                        <CardContent>
                          <Stack
                            direction={{ xs: 'column', sm: 'row' }}
                            alignItems={{ xs: 'stretch', sm: 'center' }}
                            justifyContent="space-between"
                            gap={2}
                          >
                            <Stack direction="row" spacing={2} alignItems="center">
                              <Box
                                sx={{
                                  width: 48,
                                  height: 48,
                                  borderRadius: 2,
                                  bgcolor: 'rgba(36, 107, 91, 0.1)',
                                  color: 'primary.main',
                                  display: 'grid',
                                  placeItems: 'center',
                                }}
                              >
                                {iconoCategoria(habito.categoria)}
                              </Box>
                              <Box>
                                <Typography fontWeight={800}>{habito.nombre}</Typography>
                                <Stack
                                  direction="row"
                                  spacing={1}
                                  flexWrap="wrap"
                                  rowGap={1}
                                  mt={1}
                                >
                                  <Chip size="small" label={habito.categoria} />
                                  <Chip size="small" label={habito.dificultad} />
                                  <Chip size="small" label={`Racha ${habito.rachaActual}`} />
                                  <Chip size="small" label={`${habito.puntaje} pts`} />
                                </Stack>
                              </Box>
                            </Stack>
                            <Button
                              startIcon={
                                habito.completadoHoy ? <TaskAltIcon /> : <CheckCircleIcon />
                              }
                              variant={habito.completadoHoy ? 'outlined' : 'contained'}
                              color={habito.completadoHoy ? 'success' : 'primary'}
                              disabled={habito.completadoHoy || accionandoId === habito.id}
                              onClick={() => manejarCompletar(habito)}
                              sx={{ minWidth: 136 }}
                            >
                              {accionandoId === habito.id
                                ? 'Guardando'
                                : habito.completadoHoy
                                  ? 'Completado'
                                  : 'Completar'}
                            </Button>
                          </Stack>
                        </CardContent>
                      </Card>
                    ))
                  )}
                  {!cargando && habitos.length === 0 ? (
                    <Card elevation={0} sx={{ border: '1px solid', borderColor: 'divider' }}>
                      <CardContent>
                        <Typography fontWeight={800}>Sin hábitos cargados</Typography>
                        <Typography color="text.secondary">
                          Cuando la API responda, acá van a aparecer los hábitos del jugador.
                        </Typography>
                      </CardContent>
                    </Card>
                  ) : null}
                </Stack>
              </Grid>
            </Grid>
          </Stack>
        </Container>
      </Box>

      <Dialog open={dialogoDiaAbierto} onClose={() => setDialogoDiaAbierto(false)}>
        <DialogTitle>Terminar día</DialogTitle>
        <DialogContent>
          <Typography color="text.secondary">
            Los hábitos pendientes se marcarán como fallados y la vitalidad del avatar puede bajar.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogoDiaAbierto(false)}>Cancelar</Button>
          <Button
            color="secondary"
            variant="contained"
            onClick={manejarPasarDia}
            disabled={pasandoDia}
          >
            {pasandoDia ? 'Cerrando' : 'Terminar día'}
          </Button>
        </DialogActions>
      </Dialog>

      <Snackbar
        open={Boolean(mensaje)}
        autoHideDuration={3600}
        onClose={() => setMensaje(null)}
        message={mensaje}
      />
    </ThemeProvider>
  );
}

export default App;
