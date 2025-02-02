package com.umc.yeongkkeul.aws.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
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
        metadata.setContentType(file.getContentType()); // MIME 타입 설정
        metadata.setContentLength(file.getSize());
        try {
            amazonS3.putObject(new PutObjectRequest(amazonConfig.getBucket(), keyName, file.getInputStream(), metadata));
        }catch (IOException e){
            log.error("error at AmazonS3Manager uploadFile : {}", (Object) e.getStackTrace());
        }

        return amazonS3.getUrl(amazonConfig.getBucket(), keyName).toString();
    }

    // 다운로드 기능 추가: S3의 파일을 byte[]로 반환합니다.
    public byte[] downloadFile(String keyName) {
        try {
            S3Object s3Object = amazonS3.getObject(amazonConfig.getBucket(), keyName);
            S3ObjectInputStream inputStream = s3Object.getObjectContent();
            return IOUtils.toByteArray(inputStream);
        } catch (IOException e) {
            log.error("Error downloading file from S3 with key {}: {}", keyName, e.getMessage());
            throw new RuntimeException("Failed to download file from S3", e);
        }
    }

    public String getFileUrl(String keyName) {
        return amazonS3.getUrl(amazonConfig.getBucket(), keyName).toString();
    }

    public String generateUserProfileKeyName(Long userId) {
        return amazonConfig.getUserProfilePath() + '/' + userId;
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