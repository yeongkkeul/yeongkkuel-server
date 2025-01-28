package com.umc.yeongkkeul.aws.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.umc.yeongkkeul.config.AmazonConfig;
import com.umc.yeongkkeul.domain.common.Uuid;
import com.umc.yeongkkeul.repository.UuidRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class AmazonS3Manager{

    private final AmazonS3 amazonS3;

    private final AmazonConfig amazonConfig;

    private final UuidRepository uuidRepository;


    public String uploadFile(String keyName, MultipartFile file){
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        try {
            amazonS3.putObject(new PutObjectRequest(amazonConfig.getBucket(), keyName, file.getInputStream(), metadata));
        }catch (IOException e){
            log.error("error at AmazonS3Manager uploadFile : {}", (Object) e.getStackTrace());
        }

        return amazonS3.getUrl(amazonConfig.getBucket(), keyName).toString();
    }

    public String generateUserProfileKeyName(Uuid uuid) {
        return amazonConfig.getUserProfilePath() + '/' + uuid.getUuid();
    }

    public String generateChatroomProfileKeyName(Uuid uuid) {
        return amazonConfig.getChatroomProfilePath() + '/' + uuid.getUuid();
    }

    public String generateStoreItemKeyName(Uuid uuid) {
        return amazonConfig.getStoreItemPath() + '/' + uuid.getUuid();
    }

    public String generateChatKeyName(Uuid uuid) {
        return amazonConfig.getChatPath() + '/' + uuid.getUuid();
    }
}