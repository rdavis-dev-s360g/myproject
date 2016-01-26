package com.mycompany.myproject.impl;

import com.adobe.aemfd.docmanager.Document;
import com.adobe.pdfg.result.HtmlToPdfResult;
import com.adobe.pdfg.service.api.GeneratePDFService;
import com.mycompany.myproject.HelloService;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.framework.BundleContext;

import javax.jcr.Repository;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import java.io.File;

/**
 * One implementation of the {@link HelloService}. Note that
 * the repository is injected, not retrieved.
 */
@Service
//@Component(metatype = false)
public class HelloServiceImpl implements HelloService {

    //@Reference
    private SlingRepository repository;

    //@Reference
    private GeneratePDFService generatePdfService;

    //@Reference
    private TransactionManager tm;

    public HelloServiceImpl(SlingRepository repository, GeneratePDFService generatePdfService, TransactionManager tm) {
        this.tm = tm;
        this.repository = repository;
        this.generatePdfService = generatePdfService;
    }

    public String getRepositoryName() {
        return repository.getDescriptor(Repository.REP_NAME_DESC);
    }

    private TransactionManager getTransactionManager() throws Exception {
       return tm;
    }

    public File htmlToPdf(String inputUrl, String fileTypeSettingsName, String securitySettingsName, File settingsFile, File xmpFile) throws Exception {
        Transaction tx = getTransactionManager().getTransaction();
        // Begin transaction
        if (tx == null)
            getTransactionManager().begin();
        String outputFolder = "C:/Temp";
        Document convertedDoc = null;
        Document settingsDoc = null;
        Document xmpDoc = null;
        try {
            if (settingsFile != null && settingsFile.exists() && settingsFile.isFile())
                settingsDoc = new Document(settingsFile);
            if (xmpFile != null && xmpFile.exists() && xmpFile.isFile())
                xmpDoc = new Document(xmpFile);
            HtmlToPdfResult result = generatePdfService.htmlToPdf2(inputUrl, fileTypeSettingsName, securitySettingsName, settingsDoc, xmpDoc);
            convertedDoc = result.getCreatedDocument();
            getTransactionManager().commit();
            File outputFile = new File(outputFolder, "Output.pdf");
            convertedDoc.copyToFile(outputFile);
            return outputFile;
        } catch (Exception e) {
            if (getTransactionManager().getTransaction() != null)
                getTransactionManager().rollback();
            throw e;
        } finally {
            if (convertedDoc != null) {
                convertedDoc.dispose();
                convertedDoc = null;
            }
            if (xmpDoc != null) {
                xmpDoc.dispose();
                xmpDoc = null;
            }
            if (settingsDoc != null) {
                settingsDoc.dispose();
                settingsDoc = null;
            }
        }
    }
}
