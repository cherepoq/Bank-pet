package ru.bankpet.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.*;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class VideoCompressionService {
    private final boolean enabled;
    public VideoCompressionService(@Value("${app.media.video-compression-enabled:true}") boolean enabled) { this.enabled = enabled; }

    public Optional<Path> compress(MultipartFile source) throws Exception {
        if (!enabled || !isFfmpegAvailable()) return Optional.empty();
        Path input = Files.createTempFile("rentflow-video-in-", ".upload");
        Path output = Files.createTempFile("rentflow-video-out-", ".mp4");
        try {
            source.transferTo(input);
            Process process = new ProcessBuilder("ffmpeg", "-hide_banner", "-loglevel", "error", "-y", "-i", input.toString(),
                    "-map_metadata", "-1", "-c:v", "libx264", "-preset", "veryfast", "-crf", "26", "-vf", "scale=min(1920\\,iw):-2",
                    "-c:a", "aac", "-b:a", "128k", "-movflags", "+faststart", output.toString()).redirectError(ProcessBuilder.Redirect.DISCARD).start();
            if (!process.waitFor(Duration.ofMinutes(15).toSeconds(), TimeUnit.SECONDS)) { process.destroyForcibly(); throw new IllegalStateException("Video compression timed out"); }
            if (process.exitValue() != 0 || Files.size(output) == 0) throw new IllegalArgumentException("Invalid or unsupported video");
            return Optional.of(output);
        } catch (Exception e) {
            Files.deleteIfExists(output); throw e;
        } finally { Files.deleteIfExists(input); }
    }

    private boolean isFfmpegAvailable() {
        try { return new ProcessBuilder("ffmpeg", "-version").redirectOutput(ProcessBuilder.Redirect.DISCARD).redirectError(ProcessBuilder.Redirect.DISCARD).start().waitFor(3, TimeUnit.SECONDS); }
        catch (Exception ignored) { return false; }
    }
}
