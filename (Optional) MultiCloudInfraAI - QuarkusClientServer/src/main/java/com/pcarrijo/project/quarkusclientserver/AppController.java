package com.pcarrijo.project.quarkusclientserver;


import com.pcarrijo.project.quarkusclientserver.ai.MyAiService;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;


@Path("/api/ai")
public class AppController {

    @Inject
    MyAiService myAiService;

    @POST
    @Path("/chat")
    @Produces(MediaType.TEXT_PLAIN)
    public String chatTools(String request) {
        return myAiService.chat(request);
    }

}
