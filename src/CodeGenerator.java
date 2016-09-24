import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;

import java.util.List;


/**
 * Quite a few changes here by Dallas Gutauckis [dallas@gutauckis.com]
 */
public class CodeGenerator {
    private static final String GSON_ANNOTATION_PACKAGE = "com.google.gson.annotations";
    private static final String EXPOSE_ANNOTATION_NAME = "Expose";
    private static final String SERIALIZED_NAME_ANNOTATION_NAME = "SerializedName";

    private final PsiClass mClass;
    private final List<PsiField> mFields;
    private boolean mAddExpose;
    private boolean mAddSerializedName;


    public CodeGenerator(PsiClass psiClass, List<PsiField> fields, boolean addExpose, boolean addSerializedName) {
        mClass = psiClass;
        mFields = fields;
        mAddExpose = addExpose;
        mAddSerializedName = addSerializedName;
    }

    public void generate() {
        generate(mFields);
    }


    private void generate(List<PsiField> fields) {

        try {
            JavaCodeStyleManager styleManager = JavaCodeStyleManager.getInstance(mClass.getProject());

            for (PsiField field : fields) {
                if (mAddSerializedName)
                    addSerializedNameAnnotation(field, styleManager);
                else
                    removeSerializedNameAnnotation(field);
                if (mAddExpose)
                    addExposeAnnotation(field, styleManager);
                else
                    removeExposedAnnotation(field);

                PsiClass childClass = getClass(field);
                if (childClass != null && childClass.getFields() != null && childClass.getFields().length != 0) {
                    generate(GenerateDialog.getClassFields(childClass.getFields()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private PsiClass getClass(PsiElement psiElement) {
        if (psiElement instanceof PsiVariable) {
            PsiVariable variable = (PsiVariable) psiElement;
            return getClass(variable.getType());
        } else if (psiElement instanceof PsiMethod) {
            return ((PsiMethod) psiElement).getContainingClass();
        }

        return null;
    }

    private PsiClass getClass(PsiType psiType) {
        if (psiType instanceof PsiClassType) {
            return ((PsiClassType) psiType).resolve();
        }
        return null;
    }


    private void addExposeAnnotation(PsiField psiField, JavaCodeStyleManager styleManager) {
        boolean annotated = AnnotationUtil.isAnnotated(psiField, GSON_ANNOTATION_PACKAGE + "." + EXPOSE_ANNOTATION_NAME, false) || AnnotationUtil.isAnnotated(psiField, EXPOSE_ANNOTATION_NAME, false);
        if (!annotated) {
            styleManager.shortenClassReferences(psiField.getModifierList().addAnnotation(
                    EXPOSE_ANNOTATION_NAME));
        }
    }

    private void addSerializedNameAnnotation(PsiField psiField, JavaCodeStyleManager styleManager) {
        boolean annotated = AnnotationUtil.isAnnotated(psiField, GSON_ANNOTATION_PACKAGE + "." + SERIALIZED_NAME_ANNOTATION_NAME, false) || AnnotationUtil.isAnnotated(psiField, SERIALIZED_NAME_ANNOTATION_NAME, false);
        if (!annotated) {
            styleManager.shortenClassReferences(psiField.getModifierList().addAnnotation(
                    SERIALIZED_NAME_ANNOTATION_NAME + "(\"" + psiField.getName() + "\")"));
        }
    }

    private void removeSerializedNameAnnotation(PsiField psiField) {
        boolean annotated = AnnotationUtil.isAnnotated(psiField, GSON_ANNOTATION_PACKAGE + "." + SERIALIZED_NAME_ANNOTATION_NAME, false) || AnnotationUtil.isAnnotated(psiField, SERIALIZED_NAME_ANNOTATION_NAME, false);
        if (annotated) {
            if (psiField.getModifierList() != null) {
                PsiAnnotation annotation = psiField.getModifierList().findAnnotation(GSON_ANNOTATION_PACKAGE + "." + SERIALIZED_NAME_ANNOTATION_NAME);
                if (annotation != null)
                    annotation.delete();
            }
        }
    }

    private void removeExposedAnnotation(PsiField psiField) {
        boolean annotated = AnnotationUtil.isAnnotated(psiField, GSON_ANNOTATION_PACKAGE + "." + EXPOSE_ANNOTATION_NAME, false) || AnnotationUtil.isAnnotated(psiField, EXPOSE_ANNOTATION_NAME, false);
        if (annotated) {
            if (psiField.getModifierList() != null) {
                PsiAnnotation annotation = psiField.getModifierList().findAnnotation(GSON_ANNOTATION_PACKAGE + "." + EXPOSE_ANNOTATION_NAME);
                if (annotation != null)
                    annotation.delete();
            }
        }
    }


}
