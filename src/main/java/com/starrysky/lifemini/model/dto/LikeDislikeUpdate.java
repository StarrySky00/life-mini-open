package com.starrysky.lifemini.model.dto;

import lombok.Data;

@Data
public class LikeDislikeUpdate {
        private String commentId;
        private Integer likeNum;
        private Integer dislikeNum;

        public LikeDislikeUpdate(String commentId, Integer likeNum, Integer dislikeNum) {
            this.commentId = commentId;
            this.likeNum = likeNum;
            this.dislikeNum = dislikeNum;
        }
    }