package com.thinkbiganalytics.spark;

import java.net.URI;

import javax.annotation.Nonnull;
import javax.ws.rs.core.UriBuilder;

import org.apache.spark.SparkConf;
import org.apache.spark.util.Utils;
import org.eclipse.jetty.server.Server;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.glassfish.jersey.server.ResourceConfig;

import com.thinkbiganalytics.spark.repl.ScriptEngine;
import com.thinkbiganalytics.spark.repl.ScriptEngineFactory;
import com.thinkbiganalytics.spark.rest.SparkShellController;
import com.thinkbiganalytics.spark.service.TransformService;

import scala.runtime.AbstractFunction0;
import scala.runtime.BoxedUnit;

/**
 * Instantiates a REST server for executing Spark scripts.
 */
public class SparkShellServer
{
    /**
     * Instantiates the REST server with the specified arguments.
     *
     * @param args the command-line arguments
     * @throws Exception if an error occurs
     */
    public static void main (@Nonnull final String[] args) throws Exception
    {
        // Create configuration
        ResourceConfig config = new ResourceConfig(SparkShellController.class);

        SparkConf conf = new SparkConf().setAppName("SparkShellServer");
        final ScriptEngine scriptEngine = ScriptEngineFactory.getScriptEngine(conf);
        final TransformService transformService = createTransformService(scriptEngine);
        config.register(new AbstractBinder() {
            @Override
            protected void configure () {
                bindFactory(new Factory<TransformService>() {
                    @Override
                    public void dispose (TransformService instance) {
                        // nothing to do
                    }

                    @Override
                    public TransformService provide () {
                        return transformService;
                    }
                }).to(TransformService.class).in(RequestScoped.class);
            }
        });

        // Start server
        URI bindUri = UriBuilder.fromUri("http://localhost/").port(8076).build();

        Server server = JettyHttpContainerFactory.createServer(bindUri, config);
        server.start();
        server.join();
    }

    /**
     * Create a transform service using the specified script engine.
     *
     * @param engine the script engine
     * @return the transform service
     */
    private static TransformService createTransformService (@Nonnull final ScriptEngine engine)
    {
        final TransformService service = new TransformService(engine);
        service.startAsync();

        Utils.addShutdownHook(new AbstractFunction0<BoxedUnit>() {
            @Override
            public BoxedUnit apply () {
                service.stopAsync();
                service.awaitTerminated();
                return null;
            }
        });

        service.awaitRunning();
        return service;
    }
}
