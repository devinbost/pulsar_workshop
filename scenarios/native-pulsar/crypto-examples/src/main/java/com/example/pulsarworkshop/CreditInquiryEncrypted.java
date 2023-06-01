package com.example.pulsarworkshop;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@Builder
@NonNull
@AllArgsConstructor
@NoArgsConstructor
public class CreditInquiryEncrypted {
    private Integer user_id;
    private Integer score;
    private String customer_token;
    private Long created_time;
    private String note_encrypted;
}
