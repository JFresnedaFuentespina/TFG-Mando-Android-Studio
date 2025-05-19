Sigue un modelo en el cuál dos dispositivos distintos se comunican a través de una red local utilizando sockets.
* Comunicación bidireccional:
	* ServerSocket escuchando conexiones entrantes.
	* DatagramSocket que re1aliza un broadcast y descubre el otro dispositivo en la red.
* Protocolo de comunicación personalizado:
	* Se define un protocolo simple con mensajes estructurados que permiten enviar eventos y recibir respuestas. 