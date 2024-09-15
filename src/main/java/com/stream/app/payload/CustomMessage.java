package com.stream.app.payload;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Deprecated
@Builder
public class CustomMessage {

    private String message;


    private boolean success=false;
}
