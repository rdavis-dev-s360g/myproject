package com.mycompany.myproject.impl;

import com.adobe.pdfg.service.api.GeneratePDFService;
import com.mycompany.myproject.HelloService;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.jcr.api.SlingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.transaction.TransactionManager;
import java.io.File;
import java.io.IOException;

/**
 * Test Servlet
 */
@SlingServlet(paths="/bin/PDFGServlet", methods = "GET", metatype=true)
public class PDFGServlet01 extends SlingAllMethodsServlet {
    private Logger logger = LoggerFactory.getLogger(PDFGServlet01.class);

    @Reference
    private TransactionManager tm;

    @Reference
    private SlingRepository repository;

    @Reference
    private GeneratePDFService generatePdfService;

    protected void doGet(SlingHttpServletRequest request, final SlingHttpServletResponse response)
            throws ServletException, IOException {

        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {

            HelloService helloService = new HelloServiceImpl(repository, generatePdfService, tm);
            File file = helloService.htmlToPdf("http://www.msn.com", "", "", null, null);

//            context = FrameworkUtil.getBundle(TransactionManagerLocator.class).getBundleContext();
//
//            if (context == null) {
//                response.getWriter().write("context is still null");
//            }

            response.getWriter().write("result=OK");
        } catch ( Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
