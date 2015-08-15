package org.jbei.ice.services.rest;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Paths;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import org.jbei.ice.lib.bulkupload.FileBulkUpload;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.Setting;
import org.jbei.ice.lib.dto.entry.AttachmentInfo;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.entry.SequenceInfo;
import org.jbei.ice.lib.entry.EntrySelection;
import org.jbei.ice.lib.entry.attachment.AttachmentController;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.sequence.ByteArrayWrapper;
import org.jbei.ice.lib.entry.sequence.SequenceAnalysisController;
import org.jbei.ice.lib.entry.sequence.SequenceController;
import org.jbei.ice.lib.entry.sequence.composers.pigeon.PigeonSBOLv;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.models.TraceSequence;
import org.jbei.ice.lib.net.RemoteAccessController;
import org.jbei.ice.lib.net.RemoteEntries;
import org.jbei.ice.lib.utils.EntriesAsCSV;
import org.jbei.ice.lib.utils.Utils;

/**
 * @author Hector Plahar
 */
@Path("/file")
public class FileResource extends RestResource {

    private SequenceController sequenceController = new SequenceController();
    private AttachmentController attachmentController = new AttachmentController();

    /**
     * @param fileInputStream
     * @param contentDispositionHeader
     * @return Response with attachment info on uploaded file
     */
    @POST
    @Path("attachment")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response post(@FormDataParam("file") final InputStream fileInputStream,
            @FormDataParam("file") final FormDataContentDisposition contentDispositionHeader) {
        try {
            final String fileName = contentDispositionHeader.getFileName();
            final String fileId = Utils.generateUUID();
            final File attachmentFile = Paths.get(
                    Utils.getConfigValue(ConfigurationKey.DATA_DIRECTORY),
                    AttachmentController.attachmentDirName, fileId).toFile();
            FileUtils.copyInputStreamToFile(fileInputStream, attachmentFile);
            final AttachmentInfo info = new AttachmentInfo();
            info.setFileId(fileId);
            info.setFilename(fileName);
            return Response.status(Response.Status.OK).entity(info).build();
        } catch (final IOException e) {
            Logger.error(e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves a temp file by fileId
     *
     * @param fileId
     * @return Response with temporary file
     */
    @GET
    @Path("tmp/{fileId}")
    public Response getTmpFile(@PathParam("fileId") final String fileId) {
        final File tmpFile = Paths.get(Utils.getConfigValue(ConfigurationKey.TEMPORARY_DIRECTORY),
                fileId).toFile();
        if (tmpFile == null || !tmpFile.exists()) {
            return super.respond(Response.Status.NOT_FOUND);
        }

        final Response.ResponseBuilder response = Response.ok(tmpFile);
        if (tmpFile.getName().endsWith(".csv")) {
            response.header("Content-Type", "text/csv; name=\"" + tmpFile.getName() + "\"");
        }
        response.header("Content-Disposition", "attachment; filename=\"" + tmpFile.getName() + "\"");
        return response.build();
    }

    /**
     * @param fileId
     * @return Response with attachment
     */
    @GET
    @Path("attachment/{fileId}")
    public Response getAttachment(@PathParam("fileId") final String fileId) {
        try {
            final String userId = getUserId();
            final File file = attachmentController.getAttachmentByFileId(userId, fileId);
            if (file == null) {
                return respond(Response.Status.NOT_FOUND);
            }

            String name = attachmentController.getFileName(userId, fileId);
            Response.ResponseBuilder response = Response.ok(file);
            response.header("Content-Disposition", "attachment; filename=\"" + name + "\"");
            return response.build();
        } catch (final Exception e) {
            Logger.error(e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * @param partnerId
     * @param fileId
     * @return Response with remote attachment file
     */
    @GET
    @Path("remote/{id}/attachment/{fileId}")
    public Response getRemoteAttachment(@PathParam("id") final long partnerId,
            @PathParam("fileId") final String fileId) {
        try {
        	final RemoteEntries entries = new RemoteEntries();
            final String userId = getUserId();
            final File file = entries.getPublicAttachment(userId, partnerId, fileId);
            if (file == null) {
                return respond(Response.Status.NOT_FOUND);
            }

            final Response.ResponseBuilder response = Response.ok(file);
            response.header("Content-Disposition", "attachment; filename=\"remoteAttachment\"");
            return response.build();
        } catch (final Exception e) {
            Logger.error(e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * @param type
     * @param linkedType
     * @return Response with upload CSV file
     */
    @GET
    @Path("upload/{type}")
    public Response getUploadCSV(@PathParam("type") final String type,
            @QueryParam("link") final String linkedType) {
        final EntryType entryAddType = EntryType.nameToType(type);
        final EntryType linked;
        if (linkedType != null) {
            linked = EntryType.nameToType(linkedType);
        } else {
            linked = null;
        }

        final StreamingOutput stream = new StreamingOutput() {
            @Override
            public void write(final OutputStream output) throws IOException,
            WebApplicationException {
                final byte[] template = FileBulkUpload.getCSVTemplateBytes(entryAddType, linked);
                final ByteArrayInputStream stream = new ByteArrayInputStream(template);
                IOUtils.copy(stream, output);
            }
        };

        String filename = type.toLowerCase();
        if (linkedType != null) {
            filename += ("_" + linkedType.toLowerCase());
        }

        return Response
                .ok(stream)
                .header("Content-Disposition",
                        "attachment;filename=" + filename + "_csv_upload.csv").build();
    }

    /**
     * @param partId
     * @param downloadType
     * @return Response with sequence file
     */
    @GET
    @Path("{partId}/sequence/{type}")
    public Response downloadSequence(@PathParam("partId") final long partId,
            @PathParam("type") final String downloadType) {
        final String userId = getUserId();
        final ByteArrayWrapper wrapper = sequenceController.getSequenceFile(userId, partId,
                downloadType);

        final StreamingOutput stream = new StreamingOutput() {
            @Override
            public void write(final OutputStream output) throws IOException,
            WebApplicationException {
                final ByteArrayInputStream stream = new ByteArrayInputStream(wrapper.getBytes());
                IOUtils.copy(stream, output);
            }
        };

        return Response.ok(stream)
                .header("Content-Disposition", "attachment;filename=" + wrapper.getName()).build();
    }

    /**
     * @param fileId
     * @return Response with sequence file
     */
    @GET
    @Path("trace/{fileId}")
    public Response getTraceSequenceFile(@PathParam("fileId") final String fileId) {
        try {
            final SequenceAnalysisController sequenceAnalysisController = new SequenceAnalysisController();
            final TraceSequence traceSequence = sequenceAnalysisController
                    .getTraceSequenceByFileId(fileId);
            if (traceSequence != null) {
                final File file = sequenceAnalysisController.getFile(traceSequence);
                final Response.ResponseBuilder response = Response.ok(file);
                response.header("Content-Disposition",
                        "attachment; filename=\"" + traceSequence.getFilename() + "\"");
                return response.build();
            }
            return Response.serverError().build();
        } catch (final Exception e) {
            Logger.error(e);
            return Response.serverError().build();
        }
    }

    /**
     * @param recordId
     * @return Response with image file for the SBOL
     */
    @GET
    @Produces("image/png")
    @Path("sbolVisual/{rid}")
    public Response getSBOLVisual(@PathParam("rid") final String recordId) {
        try {
            final String tmpDir = Utils.getConfigValue(ConfigurationKey.TEMPORARY_DIRECTORY);
            final Entry entry = DAOFactory.getEntryDAO().getByRecordId(recordId);
            final Sequence sequence = entry.getSequence();
            final String hash = sequence.getFwdHash();
            final File png = Paths.get(tmpDir, hash + ".png").toFile();

            if (png.exists()) {
                final Response.ResponseBuilder response = Response.ok(png);
                response.header("Content-Disposition",
                        "attachment; filename=" + entry.getPartNumber() + ".png");
                return response.build();
            }
            final URI uri = PigeonSBOLv.generatePigeonVisual(sequence);
            if (uri != null) {
                try (final InputStream in = uri.toURL().openStream();
                        final OutputStream out = new FileOutputStream(png);) {
                    IOUtils.copy(in, out);
                }
                final Response.ResponseBuilder response = Response.ok(png);
                response.header("Content-Disposition",
                        "attachment; filename=" + entry.getPartNumber() + ".png");
                return response.build();
            }
        } catch (final Exception e) {
            Logger.error(e);
            return null;
        }
        return null;
    }

    /**
     * this creates an entry if an id is not specified in the form data
     *
     * @param fileInputStream
     * @param recordId
     * @param entryType
     * @param contentDispositionHeader
     * @return Response containing sequence info
     */
    @POST
    @Path("sequence")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadSequence(@FormDataParam("file") final InputStream fileInputStream,
            @FormDataParam("entryRecordId") final String recordId,
            @FormDataParam("entryType") final String entryType,
            @FormDataParam("file") final FormDataContentDisposition contentDispositionHeader) {
        try {
            if (entryType == null) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }

            final String fileName = contentDispositionHeader.getFileName();
            final String userId = getUserId();
            final String sequence = IOUtils.toString(fileInputStream);
            final SequenceInfo sequenceInfo = sequenceController.parseSequence(userId, recordId,
                    entryType, sequence, fileName);
            if (sequenceInfo == null) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
            return Response.status(Response.Status.OK).entity(sequenceInfo).build();
        } catch (final Exception e) {
            Logger.error(e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Extracts the csv information and writes it to the temp dir and returns the file uuid. Then
     * the client is expected to make another rest call with the uuid is a separate window. This
     * workaround is due to not being able to download files using XHR or sumsuch
     *
     * @param selection
     * @return Response with filename wrapped in a Setting object
     */
    @POST
    @Path("csv")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response downloadCSV(final EntrySelection selection) {
        final String userId = getUserId();
        final EntriesAsCSV entriesAsCSV = new EntriesAsCSV();
        final boolean success = entriesAsCSV.setSelectedEntries(userId, selection);
        if (!success) {
            return super.respond(false);
        }

        final File file = entriesAsCSV.getFilePath().toFile();
        if (file.exists()) {
            return Response.ok(new Setting("key", file.getName())).build();
        }
        return respond(false);
    }
}
