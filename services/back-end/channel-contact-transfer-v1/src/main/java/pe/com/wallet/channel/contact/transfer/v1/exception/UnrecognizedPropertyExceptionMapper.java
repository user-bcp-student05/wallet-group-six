package pe.com.wallet.channel.contact.transfer.v1.exception;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import pe.com.wallet.channel.contact.transfer.v1.model.ApiError;

@Provider
public class UnrecognizedPropertyExceptionMapper implements ExceptionMapper<UnrecognizedPropertyException> {

    @Override
    public Response toResponse(UnrecognizedPropertyException exception) {
        ApiError error = new ApiError("HB0305", "La solicitud no cumple con el formato o las restricciones de los campos.");
        return Response.status(Response.Status.BAD_REQUEST)
            .type(MediaType.APPLICATION_JSON)
            .entity(error)
            .build();
    }
}
