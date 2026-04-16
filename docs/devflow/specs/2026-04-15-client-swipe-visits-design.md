# Arquitectura: Gestos Swipe para Visitas de Clientes
**Slug:** client-swipe-visits
**Tech Stack:** Android/Volley
**Problem:** Need fast way to add/remove visits via swipe gestures.
**Design:**
- Modify `ClientsFragment` ItemTouchHelper to support LEFT/RIGHT swipes.
- Swipe RIGHT (Green): POST /clients/{id}/visit
- Swipe LEFT (Red): DELETE /clients/{id}/visit (with confirmation dialog).
- Implement custom `onChildDraw` for background/icons.
- Prevent multiple swipes during processing.
