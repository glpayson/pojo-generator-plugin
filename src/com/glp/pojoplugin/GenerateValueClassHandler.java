package com.glp.pojoplugin;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiVariable;
import com.intellij.psi.impl.source.PsiClassImpl;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GenerateValueClassHandler extends EditorActionHandler {
	@Override
	protected void doExecute(
			@NotNull
					Editor editor,
			@Nullable
					Caret caret, DataContext dataContext) {
		super.doExecute(editor, caret, dataContext);

		PsiClass psiClass = generatableJavaClass(editor, dataContext);
		String className = psiClass.getName();
		if (psiClass == null)
			return;

		List<PsiVariable> variables = PsiTreeUtil.getChildrenOfAnyType(psiClass, PsiVariable.class);


		//		// TODO test
		//		rootPsiFile.accept(new PsiRecursiveElementWalkingVisitor() {
		//			@Override
		//			public void visitElement(PsiElement element) {
		//				super.visitElement(element);
		//			}
		//		});

		//		List<Variable> extractedVariables = Stream.of(rootPsiFile.getChildren())
		//				.filter(psiElement -> psiElement instanceof PsiClassImpl)
		//				.map(PsiElement::getChildren)
		//				.flatMap(Arrays::stream)
		//				.filter(psiElement -> psiElement instanceof PsiVariable)
		//				.map(psiElement -> (PsiVariable) psiElement)
		//				.map(psiVariable -> new Variable(
		//						new Type(psiVariable.getType().getCanonicalText()),
		//						new Variable.Name(psiVariable.getName())))
		//				.peek(System.out::println)
		//				.collect(Collectors.toList());
		//		if (extractedVariables.isEmpty())
		//			return;

		//		PsiClassImpl sourceJavaPsiClass = getRootClassUnderOperation(rootPsiFile);
		//		if (sourceJavaPsiClass == null)
		//			return;

				GeneratedValueWriter writeActionRunner = new GeneratedValueWriter(
						sourceJavaPsiClass, project, extractedVariables, sourceClass, rootPsiFile
				);
		//		WriteCommandAction.runWriteCommandAction(project, writeActionRunner);
	}


	private PsiClass generatableJavaClass(Editor editor, DataContext dataContext) {
		return extractPsiClasses(editor, dataContext)
				.stream()
				.filter(this::isJavaClassWithVariables)
				.findFirst()
				.orElse(null);
	}

	private List<PsiClass> extractPsiClasses(Editor editor, DataContext dataContext) {
		return Optional.ofNullable(CommonDataKeys.PROJECT.getData(dataContext))
				.map(x -> PsiUtilBase.getPsiFileInEditor(editor, x))
				.map(x -> PsiTreeUtil.getChildrenOfAnyType(x, PsiClass.class))
				.orElse(Collections.emptyList());
	}

	private boolean isJavaClassWithVariables(PsiClass psiClass) {
		return psiClass.getLanguage().is(JavaLanguage.INSTANCE) &&
				!PsiTreeUtil.getChildrenOfAnyType(psiClass, PsiVariable.class).isEmpty();
	}

//	private boolean canNotGenerate(Project project, Editor editor, Caret caret,
//			DataContext dataContext) {
//		return project == null ||
//				languageIsNotJava(editor, project) ||
//				psiFileIsNull(editor, project) ||
//				classHasNoFields() ||
//				psiClassIsNull();
//
//	}
//
//	private boolean psiFileIsNull(Editor editor, Project project) {
//		return PsiUtilBase.getPsiFileInEditor(editor, project) == null;
//	}
//
//	private boolean languageIsNotJava(Editor editor, Project project) {
//		Language languageInEditor = PsiUtilBase.getLanguageInEditor(editor, project);
//		return languageInEditor == null || !languageInEditor.is(JavaLanguage.INSTANCE);
//	}
//
//	//TODO: Implement
//	private boolean classHasNoFields() {
//		return false;
//	}
//
//	//TODO: Implement
//	private boolean psiClassIsNull() {
//		return false;
//	}

	@Nullable
	private static PsiClassImpl getRootClassUnderOperation(PsiFile psiFile) {
		return Stream.of(psiFile.getChildren())
				.filter(psiElement -> psiElement instanceof PsiClassImpl)
				.map(psiElement -> (PsiClassImpl) psiElement)
				.findFirst()
				.orElse(null);
	}
}
