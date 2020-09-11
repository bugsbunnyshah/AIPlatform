package io.bugsbunny.endpoint;

import org.bytedeco.librealsense.context;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import java.io.IOException;

@Provider
public class AITrafficAgent implements ContainerRequestFilter
{
    @Override
    public void filter(ContainerRequestContext context) throws IOException
    {
        context.abortWith(Response.accepted("forbidden!").build());
    }
}
