package mr.intellij.plugin.autofactory.inspections.direct.instantiation;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.psi.PsiNewExpression;
import lombok.RequiredArgsConstructor;
import mr.intellij.plugin.autofactory.utils.AnnotationUtils;
import mr.intellij.plugin.autofactory.utils.ProjectFilesUtils;

/**
 * @see DirectInstantiationInspection
 */
@RequiredArgsConstructor
class DirectInstantiationVisitor extends JavaElementVisitor {

    private static final String DESCRIPTION_TEMPLATE = "Using #ref instead of %sFactory.create(...)";

    private final ProblemsHolder holder;

    @Override
    public void visitNewExpression(PsiNewExpression expression) {
        super.visitNewExpression(expression);

        PsiJavaCodeReferenceElement reference = expression.getClassReference();
        if (reference == null) {
            return;
        }

        PsiClass instantiatedClass = (PsiClass) reference.resolve();
        if (instantiatedClass == null) {
            return;
        }

        if (AnnotationUtils.hasAutoFactory(expression.resolveConstructor(), true)
            && isRelevant(expression, instantiatedClass)) {

            String problemDescription = String.format(DESCRIPTION_TEMPLATE, instantiatedClass.getName());
            holder.registerProblem(expression, problemDescription, (LocalQuickFix) null);
        }
    }

    /**
     * @return True if the expression was not made in a test and if the instantiated class was not declared in a test
     * file, too.
     * The rational of this is to allow direct instantiation in tests, but only for classes being tested or
     * collaborators, and not classes declared in tests, like utils.
     */
    private boolean isRelevant(PsiNewExpression expression, PsiClass instantiatedClass) {
        return !ProjectFilesUtils.isInTestFile(expression) || ProjectFilesUtils.isInTestFile(instantiatedClass);
    }
}
