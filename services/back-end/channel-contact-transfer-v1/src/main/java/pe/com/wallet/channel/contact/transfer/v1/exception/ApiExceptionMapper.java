package pe.com.wallet.channel.contact.transfer.v1.exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import pe.com.wallet.channel.contact.transfer.v1.model.ApiError;

@Provider
public class ApiExceptionMapper implements ExceptionMapper<ApiException> {

    @Override
    public Response toResponse(ApiException exception) {
        ApiError error = new ApiError(exception.getCode(), exception.getDescription());
        return Response.status(exception.getStatus())
            .type(MediaType.APPLICATION_JSON)
            .entity(error)
            .build();
    }
}
