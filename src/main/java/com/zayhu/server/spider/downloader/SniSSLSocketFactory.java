package com.zayhu.server.spider.downloader;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.protocol.HttpContext;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.Socket;

public class SniSSLSocketFactory extends SSLConnectionSocketFactory {
    public static final String ENABLE_SNI = "__enable_sni__";

    public SniSSLSocketFactory(final SSLContext sslContext, final HostnameVerifier verifier) {
        super(sslContext, verifier);
    }

    @Override
    public Socket createLayeredSocket(
            final Socket socket,
            final String target,
            final int port,
            final HttpContext context) throws IOException {
        Boolean enableSniValue = (Boolean) context.getAttribute(ENABLE_SNI);
        boolean enableSni = enableSniValue == null || enableSniValue;
        return super.createLayeredSocket(socket, enableSni ? target : "", port, context);
    }
}
