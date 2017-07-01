package mr.intellij.plugin.autofactory.inspections.conflicting.constructor;

import com.google.gson.Gson;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiMethod;
import com.intellij.testFramework.fixtures.*;
import mr.intellij.plugin.autofactory.TestUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link ConflictingConstructorVisitor}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ConflictingConstructorVisitorTest {

    private static JavaCodeInsightTestFixture codeInsightFixture;
    private static PsiClass samplePsiClass;

    @Captor private ArgumentCaptor<PsiElement> psiElementCaptor;
    @Mock private ProblemsHolder mockProblemsHolder;
    @InjectMocks private ConflictingConstructorVisitor tested;

    @BeforeClass
    public static void setupClass() throws Exception {
        TestFixtureBuilder<IdeaProjectTestFixture> projectBuilder = IdeaTestFixtureFactory.getFixtureFactory()
                                                                                          .createFixtureBuilder("");

        IdeaProjectTestFixture projectFixture = projectBuilder.getFixture();
        codeInsightFixture = JavaTestFixtureFactory.getFixtureFactory()
                                                   .createCodeInsightFixture(projectFixture);
        codeInsightFixture.setUp();
        samplePsiClass = codeInsightFixture.addClass(TestUtils.loadResource("ClassWithConflictingConstructors.java"));
    }

    @AfterClass
    public static void teardownClass() throws Exception {
        codeInsightFixture.tearDown();
    }

    @Test
    public void testVisitClass() {
        final boolean[] run = {false};

        ApplicationManager.getApplication().runReadAction(() -> {
            tested.visitClass(samplePsiClass);

            verify(mockProblemsHolder, times(2)).registerProblem(psiElementCaptor.capture(),
                                                                 eq(ConflictingConstructorInspection.DESCRIPTION),
                                                                 eq(ProblemHighlightType.GENERIC_ERROR));

            assertThat(psiElementCaptor.getAllValues())
                    .containsExactlyElementsOf(Arrays.stream(samplePsiClass.getConstructors())
                                                     .map(PsiMethod::getNameIdentifier)
                                                     .collect(Collectors.toList()));
            run[0] = true;
        });

        // Sanity check that the read action did run.
        assertThat(run[0]).isTrue();
    }
}
