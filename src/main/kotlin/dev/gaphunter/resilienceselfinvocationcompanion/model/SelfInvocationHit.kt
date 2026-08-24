package dev.gaphunter.resilienceselfinvocationcompanion.model

import com.intellij.psi.PsiElement

/** One self-invocation call site of a resilience-annotated method from within its own declaring class. */
data class SelfInvocationHit(val methodName: String, val annotationName: String, val callElement: PsiElement)
