Componentes Principales:
1. Aplicación Android (Mando):
	1. Módulo de interfaz:
		1. Layouts específicos para distintos tipos de juego (joystick o volante).
		2. Elementos interactivos: joystick, botones de acción, menú de configuración.
	2. Módulo de personalización:
		1. Permite modificar la posición y el tamaño de los controles.
		2. Guarda preferencias del usuario para cada tipo de layout.
	3. Módulo de comunicación:
		1. Establece la conexión con el juego en Java.
		2. Envía instrucciones del usuario en tiempo real.
		3. Escucha mensajes del juego.
2. Aplicación Java (Juego):
	1. Motor del juego:
		1. Simula el movimiento de objetos.
		2. Controla la lógica del juego como colisiones, disparos, vida, puntuación...
		3. Se adapta al layout recibido desde Android.
	2. Módulo de comunicación:
		1. Recibe instrucciones enviadas desde el mando Android.
		2. Responde con el estado del juego, eventos importantes o peticiones (reinicio, salida...).
	3. Interfaz gráfica:
		1. Representación visual del juego mediante Java.
