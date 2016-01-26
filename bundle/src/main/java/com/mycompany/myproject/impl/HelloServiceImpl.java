package com.mycompany.myproject.impl;

import com.adobe.aemfd.docmanager.Document;
import com.adobe.pdfg.result.HtmlToPdfResult;
import com.adobe.pdfg.service.api.GeneratePDFService;
import com.mycompany.myproject.HelloService;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.jcr.api.SlingRepository;

import javax.jcr.Repository;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import java.io.File;

/**
 * Simple class that calls PDFG
 */
public class HelloServiceImpl implements HelloService {

    private SlingRepository repository;
    private GeneratePDFService generatePdfService;
    private TransactionManager tm;

    /**
     * Ctor
     * @param repository
     * @param generatePdfService
     * @param tm
     */
    public HelloServiceImpl(SlingRepository repository, GeneratePDFService generatePdfService, TransactionManager tm) {
        this.tm = tm;
        this.repository = repository;
        this.generatePdfService = generatePdfService;
    }

    /**
     * Repo name
     *
     * @return repository name
     */
    public String getRepositoryName() {
        return repository.getDescriptor(Repository.REP_NAME_DESC);
    }

    // Helper to get the TM
    private TransactionManager getTransactionManager() throws Exception {
       return tm;
    }

    /**
     * Convert web resource to PDF
     *
     * @param inputUrl
     * @param fileTypeSettingsName
     * @param securitySettingsName
     * @param settingsFile
     * @param xmpFile
     * @return
     * @throws Exception
     */
    public File htmlToPdf(String inputUrl, String fileTypeSettingsName, String securitySettingsName, File settingsFile, File xmpFile) throws Exception {

        // Get a transaction object
        Transaction tx = getTransactionManager().getTransaction();

        // Begin transaction if we don't have one in flight already
        if (tx == null) {
            getTransactionManager().begin();
        }

        String outputFolder = "C:/Temp";
        Document convertedDoc = null;
        Document settingsDoc = null;
        Document xmpDoc = null;

        try {

            // If a settings xml file was passed, convert to a Document
            if (settingsFile != null && settingsFile.exists() && settingsFile.isFile()) {
                settingsDoc = new Document(settingsFile);
            }

            // If a XMP xml file was passed, convert to a Document
            if (xmpFile != null && xmpFile.exists() && xmpFile.isFile())
                xmpDoc = new Document(xmpFile);

            // Call OSGI service
            HtmlToPdfResult result = generatePdfService.htmlToPdf2(inputUrl, fileTypeSettingsName, securitySettingsName, settingsDoc, xmpDoc);

            // Get generated document
            convertedDoc = result.getCreatedDocument();

            // Commit transaction
            getTransactionManager().commit();

            // Right out the file and return
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
