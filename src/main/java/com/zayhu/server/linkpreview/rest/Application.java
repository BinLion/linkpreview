package com.zayhu.server.linkpreview.rest;

import com.google.inject.Injector;
import com.yeecall.yeetoken.service.config.YeeTokenConfigService;
import com.zayhu.server.httpapi.rest.guice.FinderFactory;
import com.zayhu.server.httpapi.rest.guice.RestletGuice;
import com.zayhu.server.httpapi.ws.AbstractWSHandler;
import com.zayhu.server.linkpreview.guice.GuiceModule;
import com.zayhu.server.linkpreview.rest.api.LinkPreviewServerResource;
import com.zayhu.server.linkpreview.rest.api.inner.LinkPreviewInnerServerResource;
import com.zayhu.server.util.RestletLoggerUtils;
import org.apache.commons.configuration.Configuration;
import org.restlet.Context;
import org.restlet.routing.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: daisyw
 * @data: 2019/4/27 下午1:55
 */
public class Application extends com.yeecall.yeetoken.yeeapi.rest.Application {

    protected static Logger logger = LoggerFactory.getLogger(Application.class);

    public Application(Injector injector) {
        super(injector);
        usingUserAndUidComponet = false;
        this.injector = injector;
    }

    public Application(Context context) {
        super(context);
        usingUserAndUidComponet = false;
    }

    @Override
    public void loadBeforeStart() throws Exception {
        super.loadBeforeStart();
        injector.getInstance(YeeTokenConfigService.class);

    }

    protected String[] allNeedIndexModelPackageNames() {
        return new String[]{"com.yeecall.yeetoken.yeeapi.rest.model"};
    }

    @Override
    public void start() throws Exception {
        super.start();
        AbstractWSHandler.retryWhen502 = true;
    }

    @Override
    public void stop() throws Exception {
        super.stop();
    }

    protected void addApis(FinderFactory ff, Router router) {
        super.addApis(ff, router);
        router.attach("/link/{method}",ff.finder(LinkPreviewServerResource.class));
    }


    protected void addInnerApis(FinderFactory ff, Router router) {
        super.addInnerApis(ff, router);
        router.attach("/linkpreview/_inner/link/{method}",ff.finder(LinkPreviewInnerServerResource.class));
    }

    public static void main(String[] args) throws Exception {
        RestletLoggerUtils.changeLogger("org.restlet.engine.loggerFacadeClass", "org.restlet.ext.slf4j.Slf4jLoggerFacade");
        Injector injector = RestletGuice.createInjector(new GuiceModule());
        int ttl = injector.getInstance(Configuration.class).getInt("network.address.cache.ttl", 30);
        java.security.Security.setProperty("networkaddress.cache.ttl", String.valueOf(ttl));
        startApplication(new Application(injector) {
        }, "linkpreview", "");
    }

    public static Application getInstaceOrStartMain(String [] args) throws Exception {
        if (getInstanceMybeNull() == null) {
            main(args);
        }
        return (Application) getInstance();
    }
}
