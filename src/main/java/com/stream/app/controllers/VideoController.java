package com.stream.app.controllers;

import com.stream.app.AppConstants;
import com.stream.app.entities.Video;
import com.stream.app.payload.CustomMessage;
import com.stream.app.services.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/videos")
@CrossOrigin("*")
public class VideoController {


    @Autowired
    private VideoService videoService;



    @PostMapping()
    public ResponseEntity<?> create(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("description") String description
    ) {

        Video video = new Video();
        video.setTitle(title);
        video.setDescription(description);
        video.setVideoId(UUID.randomUUID().toString());
        Video saveVideo = videoService.saveVideo(video, file);

        if (saveVideo != null) {
            return ResponseEntity.status(HttpStatus.OK).body(video);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new CustomMessage("Video upload Failed", false)
            );
        }
    }

    @GetMapping("/stream/{videoId}")
    public ResponseEntity<Resource> stream(@PathVariable("videoId") String videoId) {
        Video video = this.videoService.getVideo(videoId);

        String contentType = video.getContentType();
        String filePath = video.getFilePath();

        Resource resource = new FileSystemResource(filePath);

        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
                .body(resource);


    }

    @GetMapping
    public ResponseEntity<List<Video>> gatAllVideos() {

        List<Video> allVideos = this.videoService.getAllVideos();

        return ResponseEntity.status(HttpStatus.OK).body(allVideos);

    }


    //stream video in chunks

    @GetMapping("/stream/range/{videoId}")
    public ResponseEntity<Resource> streamVideoRange(
            @PathVariable("videoId") String videoId,
            @RequestHeader(value = "Range", required = false) String range
    ) {

        System.out.println(range);
        Video video = this.videoService.getVideo(videoId);
        Path path = Paths.get(video.getFilePath());

        Resource resource = new FileSystemResource(path);
        String contentType = video.getContentType();
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        long fileLength = path.toFile().length();

        if (range == null) {
            return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType)).body(resource);
        }

        long rangeStart;
        long rangeEnd;

        String[] ranges = range.replace("bytes=", "").split("-");

        rangeStart = Long.parseLong(ranges[0]);
        rangeEnd=rangeStart+ AppConstants.CHUNK_SIZE-1;

        if (rangeEnd>= fileLength)
        {
            rangeEnd=fileLength-1;
        }


//        if (ranges.length > 1) {
//            rangeEnd = Long.parseLong(ranges[1]);
//        } else {
//            rangeEnd = fileLength - 1;
//        }
//
//        if (rangeEnd > fileLength - 1) {
//            rangeEnd = fileLength - 1;
//        }

        InputStream inputStream;

        try {
            inputStream = Files.newInputStream(path);
            inputStream.skip(rangeStart);

            long contentLength = rangeEnd - rangeStart + 1;

            byte[] data=new byte[(int)contentLength];

            int read = inputStream.read(data, 0, data.length);

            System.out.println("read(number of bytes): " +read);


            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Range", "bytes " + rangeStart + "-" + rangeEnd + "/" + fileLength);
            headers.add("Cache-Control", "no-cache, no-store , must-revalidate");
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");
            headers.add("X-Content-Type-Options", "nosniff");

            headers.setContentLength(contentLength);


            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .headers(headers)
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(new ByteArrayResource(data));


        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }


    }

    //first download ffmpeg to local or server

    //serve playlist

    //master .m2u8 file

    @Value("$file.video.hsl}")
    private String HSL_DIR;

    @GetMapping("/{videoId}/master.m3u8")
    private ResponseEntity<Resource> serverMasterFile(
            @PathVariable("videoId") String videoId
    )
    {

        //creating path

        Path path=Paths.get(HSL_DIR,videoId,"master.m3u8");
        System.out.println(path);
        if (!Files.exists(path))
        {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Resource resource=new FileSystemResource(path);
        HttpHeaders headers=new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE,"application/vnd.apple.mpegurl");

        return ResponseEntity.ok()
                .headers( headers)
                .body(resource);

    }

    //serve the segments

    @GetMapping("/{videoId}/{segment}.ts")
    public ResponseEntity<Resource> serveSegment(
            @PathVariable("videoId") String videoId,
            @PathVariable("segment") String segment
    )
    {

        Path path=Paths.get(HSL_DIR,videoId,segment+".ts");
        if (!Files.exists(path))
        {
            return  new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }


        Resource resource=new FileSystemResource(path);

        HttpHeaders headers=new HttpHeaders();

        headers.add(HttpHeaders.CONTENT_TYPE,"video/mp2t");

        return ResponseEntity.ok().headers(headers).body(resource);





    }



}
