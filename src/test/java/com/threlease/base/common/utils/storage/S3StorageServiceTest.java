package com.threlease.base.common.utils.storage;

import com.threlease.base.common.exception.BusinessException;
import com.threlease.base.common.properties.aws.s3.S3Properties;
import com.threlease.base.common.utils.storage.entity.FileEntity;
import com.threlease.base.common.utils.storage.repository.FileRepository;
import io.awspring.cloud.s3.S3Template;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class S3StorageServiceTest {

    @Test
    void deleteFailsWhenS3DeleteFails() {
        S3Template s3Template = mock(S3Template.class);
        FileRepository fileRepository = mock(FileRepository.class);
        S3Properties s3Properties = new S3Properties();
        s3Properties.setBucket("bucket");
        FileUploadSecurityService uploadSecurityService = mock(FileUploadSecurityService.class);
        org.springframework.beans.factory.ObjectProvider<software.amazon.awssdk.services.s3.presigner.S3Presigner> provider = mock(org.springframework.beans.factory.ObjectProvider.class);

        S3StorageService service = new S3StorageService(s3Template, fileRepository, s3Properties, uploadSecurityService, provider);

        doThrow(new RuntimeException("boom")).when(s3Template).deleteObject("bucket", "path/to/file.txt");

        FileEntity file = FileEntity.builder().filePath("path/to/file.txt").build();

        assertThrows(BusinessException.class, () -> service.delete(file.getFilePath()));
        verify(fileRepository, never()).findByFilePathAndDeletedFalse("path/to/file.txt");
    }
}
