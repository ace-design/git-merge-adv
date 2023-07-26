package org.apache.maven.cli.transfer;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import org.eclipse.aether.transfer.AbstractTransferListener;
import org.eclipse.aether.transfer.TransferCancelledException;
import org.eclipse.aether.transfer.TransferEvent;
import org.eclipse.aether.transfer.TransferResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Slf4jMavenTransferListener extends AbstractTransferListener {
    protected final Logger out;

    public Slf4jMavenTransferListener() {
        this.out = LoggerFactory.getLogger(Slf4jMavenTransferListener.class);
    }

    // TODO should we deprecate?
    public Slf4jMavenTransferListener(Logger out) {
        this.out = out;
    }

    @Override
    public void transferInitiated(TransferEvent event) {
        String message = (event.getRequestType() == TransferEvent.RequestType.PUT) ? "Uploading" : "Downloading";
        out.info(((message + ": ") + event.getResource().getRepositoryUrl()) + event.getResource().getResourceName());
    }

    @Override
    public void transferCorrupted(TransferEvent event) throws TransferCancelledException {
        TransferResource resource = event.getResource();
        out.warn(((event.getException().getMessage() + " for ") + resource.getRepositoryUrl()) + resource.getResourceName());
    }

    @Override
    public void transferSucceeded(TransferEvent event) {
        TransferResource resource = event.getResource();
        long contentLength = event.getTransferredBytes();
        if (contentLength >= 0) {
            String type = (event.getRequestType() == TransferEvent.RequestType.PUT) ? "Uploaded" : "Downloaded";
            String len = (contentLength >= 1024) ? toKB(contentLength) + " KB" : contentLength + " B";
            String throughput = "";
            long duration = System.currentTimeMillis() - resource.getTransferStartTime();
            if (duration > 0) {
                DecimalFormat format = new DecimalFormat("0.0", new DecimalFormatSymbols(Locale.ENGLISH));
                double kbPerSec = (contentLength / 1024.0) / (duration / 1000.0);
                throughput = (" at " + format.format(kbPerSec)) + " KB/sec";
            }
            out.info(((((((type + ": ") + resource.getRepositoryUrl()) + resource.getResourceName()) + " (") + len) + throughput) + ")");
        }
    }

    protected long toKB( long bytes )
    {
        return ( bytes + 1023 ) / 1024;
    }
}
