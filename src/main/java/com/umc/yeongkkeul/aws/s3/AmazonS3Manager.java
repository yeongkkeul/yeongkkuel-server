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

    /**
     * S3에서 파일을 다운로드하며, 파일 데이터와 함께 원본의 contentType를 반환합니다.
     */
    public S3DownloadResponse downloadFileWithMetadata(String keyName) {
        try {
            S3Object s3Object = amazonS3.getObject(amazonConfig.getBucket(), keyName);
            S3ObjectInputStream inputStream = s3Object.getObjectContent();
            byte[] data = IOUtils.toByteArray(inputStream);
            // S3 객체의 메타데이터에서 콘텐츠 타입을 가져옵니다.
            String contentType = s3Object.getObjectMetadata().getContentType();
            return new S3DownloadResponse(data, contentType);
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

    public String generateExpenseImageKeyName(Uuid uuid) {
        return amazonConfig.getExpenseImagePath() + '/' + uuid.getUuid();
    }

    /**
     * S3 다운로드 결과를 담는 DTO. -> 추후 다운로드 api에서 메타데이터 넣는 활용 필요해서 추가함
     */
    public static class S3DownloadResponse {
        private final byte[] data;
        private final String contentType;

        public S3DownloadResponse(byte[] data, String contentType) {
            this.data = data;
            this.contentType = contentType;
        }

        public byte[] getData() {
            return data;
        }

        public String getContentType() {
            return contentType;
        }
    }
}