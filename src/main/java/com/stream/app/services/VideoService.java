package com.stream.app.services;

import com.stream.app.entities.Video;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;


@Service
public interface VideoService {

    //save video

    public Video saveVideo(Video video, MultipartFile file);

    //get video by id

    public Video getVideo(String videoId);


    //get video by title

    public Video getVideoByTitle(String videoTitle);

    //get all videos

    public List<Video> getAllVideos();


    //video processing

    public String processVideo(String videoId);
}
