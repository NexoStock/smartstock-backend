# Entrega de notificaciones

Las notificaciones están desactivadas por defecto.
Para habilitarlas, configurar app.notifications.enabled=true
y las credenciales de los proveedores correspondientes.

## Posible reenvío

Si SMTP o Twilio acepta un mensaje y el proceso falla antes de
guardar el estado SENT, el mensaje puede volver a enviarse al
recuperarse para su procesamiento.

Por ello, no se garantiza una entrega exactamente una vez:
el destinatario podría recibir una notificación duplicada.

## Prueba sin SMTP

Ejecutar:

    .\mvnw.cmd "-Dtest=NotificationsDisabledContextTest" test

Estas pruebas verifican el arranque del contexto de notificaciones
sin JavaMailSender, con notificaciones desactivadas explícitamente
y con la propiedad de activación ausente.
No sustituyen una prueba de arranque completo del backend.
