package inu.codin.codin.domain.post.scheduler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.lang.reflect.Method;

import inu.codin.codin.domain.post.scheduler.exception.SchedulerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PostsSchedulerTest {

    @InjectMocks
    private PostsScheduler postsScheduler;

    @Mock
    private Process process;

    private static final String TEST_PATH = "/test/path/";
    private static final String TEST_PYTHON_DIR = "/usr/bin/python3";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(postsScheduler, "PATH", TEST_PATH);
        ReflectionTestUtils.setField(postsScheduler, "PYTHON_DIR", TEST_PYTHON_DIR);
    }

    @Test
    @DisplayName("학과 공지사항 스케줄러 정상 실행")
    void departmentPostsScheduler_Success() throws Exception {
        // Given
        try (MockedConstruction<ProcessBuilder> processBuilderMock = mockConstruction(ProcessBuilder.class,
                (mock, context) -> {
                    when(mock.inheritIO()).thenReturn(mock);
                    when(mock.command(anyString(), anyString())).thenReturn(mock);
                    when(mock.start()).thenReturn(process);
                })) {

            when(process.waitFor()).thenReturn(0);

            // When
            postsScheduler.departmentPostsScheduler();

            // Then
            assertEquals(1, processBuilderMock.constructed().size());
            ProcessBuilder constructedBuilder = processBuilderMock.constructed().get(0);
            verify(constructedBuilder).inheritIO();
            verify(constructedBuilder).command(TEST_PYTHON_DIR, TEST_PATH + "department.py");
            verify(constructedBuilder).start();
            verify(process).waitFor();
        }
    }

    @Test
    @DisplayName("STARINU 공지사항 스케줄러 정상 실행")
    void starinuPostsScheduler_Success() throws Exception {
        // Given
        try (MockedConstruction<ProcessBuilder> processBuilderMock = mockConstruction(ProcessBuilder.class,
                (mock, context) -> {
                    when(mock.inheritIO()).thenReturn(mock);
                    when(mock.command(anyString(), anyString())).thenReturn(mock);
                    when(mock.start()).thenReturn(process);
                })) {

            when(process.waitFor()).thenReturn(0);

            // When
            postsScheduler.starinuPostsScheduler();

            // Then
            assertEquals(1, processBuilderMock.constructed().size());
            ProcessBuilder constructedBuilder = processBuilderMock.constructed().get(0);
            verify(constructedBuilder).inheritIO();
            verify(constructedBuilder).command(TEST_PYTHON_DIR, TEST_PATH + "starinu.py");
            verify(constructedBuilder).start();
            verify(process).waitFor();
        }
    }

    @Test
    @DisplayName("Python 스크립트 실행 성공 - 정상 종료 코드")
    void runPythonScript_Success_ExitCode0() throws Exception {
        // Given
        try (MockedConstruction<ProcessBuilder> processBuilderMock = mockConstruction(ProcessBuilder.class,
                (mock, context) -> {
                    when(mock.inheritIO()).thenReturn(mock);
                    when(mock.command(anyString(), anyString())).thenReturn(mock);
                    when(mock.start()).thenReturn(process);
                })) {

            when(process.waitFor()).thenReturn(0);

            // When
            Method runPythonScript = PostsScheduler.class.getDeclaredMethod("runPythonScript", String.class, String.class);
            runPythonScript.setAccessible(true);

            assertDoesNotThrow(() -> {
                runPythonScript.invoke(postsScheduler, "test.py", "테스트 태스크");
            });

            // Then
            verify(process).waitFor();
        }
    }

    @Test
    @DisplayName("Python 스크립트 실행 실패 - 비정상 종료 코드")
    void runPythonScript_Failure_NonZeroExitCode() throws Exception {
        // Given
        try (MockedConstruction<ProcessBuilder> processBuilderMock = mockConstruction(ProcessBuilder.class,
                (mock, context) -> {
                    when(mock.inheritIO()).thenReturn(mock);
                    when(mock.command(anyString(), anyString())).thenReturn(mock);
                    when(mock.start()).thenReturn(process);
                })) {

            when(process.waitFor()).thenReturn(1); // 비정상 종료 코드

            // When
            Method runPythonScript = PostsScheduler.class.getDeclaredMethod("runPythonScript", String.class, String.class);
            runPythonScript.setAccessible(true);

            // 비정상 종료 코드여도 예외가 발생하지 않고 로그만 출력됨
            assertDoesNotThrow(() -> {
                runPythonScript.invoke(postsScheduler, "test.py", "테스트 태스크");
            });

            // Then
            verify(process).waitFor();
        }
    }

    @Test
    @DisplayName("Python 스크립트 실행 중 IOException 발생")
    void runPythonScript_IOException() throws Exception {
        // Given
        try (MockedConstruction<ProcessBuilder> processBuilderMock = mockConstruction(ProcessBuilder.class,
                (mock, context) -> {
                    when(mock.inheritIO()).thenReturn(mock);
                    when(mock.command(anyString(), anyString())).thenReturn(mock);
                    when(mock.start()).thenThrow(new IOException("테스트 IOException"));
                })) {

            // When & Then
            Method runPythonScript = PostsScheduler.class.getDeclaredMethod("runPythonScript", String.class, String.class);
            runPythonScript.setAccessible(true);

            Exception exception = assertThrows(Exception.class, () -> {
                runPythonScript.invoke(postsScheduler, "test.py", "테스트 태스크");
            });

            // InvocationTargetException이 발생하므로 원인을 확인
            assertTrue(exception.getCause() instanceof SchedulerException);
        }
    }

    @Test
    @DisplayName("Python 스크립트 실행 중 InterruptedException 발생")
    void runPythonScript_InterruptedException() throws Exception {
        // Given
        try (MockedConstruction<ProcessBuilder> processBuilderMock = mockConstruction(ProcessBuilder.class,
                (mock, context) -> {
                    when(mock.inheritIO()).thenReturn(mock);
                    when(mock.command(anyString(), anyString())).thenReturn(mock);
                    when(mock.start()).thenReturn(process);
                })) {

            when(process.waitFor()).thenThrow(new InterruptedException("테스트 InterruptedException"));

            // When & Then
            Method runPythonScript = PostsScheduler.class.getDeclaredMethod("runPythonScript", String.class, String.class);
            runPythonScript.setAccessible(true);

            Exception exception = assertThrows(Exception.class, () -> {
                runPythonScript.invoke(postsScheduler, "test.py", "테스트 태스크");
            });

            // InvocationTargetException이 발생하므로 원인을 확인
            assertTrue(exception.getCause() instanceof SchedulerException);
        }
    }

    @Test
    @DisplayName("스케줄러 어노테이션 설정 검증")
    void validateSchedulerAnnotations() throws Exception {
        // Given
        Class<?> clazz = PostsScheduler.class;

        // When & Then - departmentPostsScheduler 메서드 어노테이션 검증
        Method departmentMethod = clazz.getMethod("departmentPostsScheduler");
        assertTrue(departmentMethod.isAnnotationPresent(Scheduled.class));
        assertTrue(departmentMethod.isAnnotationPresent(Async.class));

        Scheduled departmentScheduled = departmentMethod.getAnnotation(Scheduled.class);
        assertEquals("${schedule.department.cron}", departmentScheduled.cron());
        assertEquals("Asia/Seoul", departmentScheduled.zone());

        // starinuPostsScheduler 메서드 어노테이션 검증
        Method starinuMethod = clazz.getMethod("starinuPostsScheduler");
        assertTrue(starinuMethod.isAnnotationPresent(Scheduled.class));
        assertTrue(starinuMethod.isAnnotationPresent(Async.class));

        Scheduled starinuScheduled = starinuMethod.getAnnotation(Scheduled.class);
        assertEquals("${schedule.starinu.cron}", starinuScheduled.cron());
        assertEquals("Asia/Seoul", starinuScheduled.zone());
    }

    @Test
    @DisplayName("프로퍼티 값 주입 테스트")
    void validatePropertyInjection() {
        // Given & When
        String path = (String) ReflectionTestUtils.getField(postsScheduler, "PATH");
        String pythonDir = (String) ReflectionTestUtils.getField(postsScheduler, "PYTHON_DIR");

        // Then
        assertEquals(TEST_PATH, path);
        assertEquals(TEST_PYTHON_DIR, pythonDir);
    }

    @Test
    @DisplayName("ProcessBuilder 체이닝 메서드 호출 순서 검증")
    void validateProcessBuilderMethodChaining() throws Exception {
        // Given
        try (MockedConstruction<ProcessBuilder> processBuilderMock = mockConstruction(ProcessBuilder.class,
                (mock, context) -> {
                    when(mock.inheritIO()).thenReturn(mock);
                    when(mock.command(anyString(), anyString())).thenReturn(mock);
                    when(mock.start()).thenReturn(process);
                })) {

            when(process.waitFor()).thenReturn(0);

            // When
            Method runPythonScript = PostsScheduler.class.getDeclaredMethod("runPythonScript", String.class, String.class);
            runPythonScript.setAccessible(true);
            runPythonScript.invoke(postsScheduler, "test.py", "테스트 태스크");

            // Then - 메서드 호출 순서 검증
            ProcessBuilder constructedBuilder = processBuilderMock.constructed().get(0);
            InOrder inOrder = inOrder(constructedBuilder, process);
            inOrder.verify(constructedBuilder).inheritIO();
            inOrder.verify(constructedBuilder).command(TEST_PYTHON_DIR, TEST_PATH + "test.py");
            inOrder.verify(constructedBuilder).start();
            inOrder.verify(process).waitFor();
        }
    }
}