package dev.gaphunter.resilienceselfinvocationcompanion.detect

import com.intellij.psi.JavaRecursiveElementWalkingVisitor
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiMethodCallExpression
import dev.gaphunter.resilienceselfinvocationcompanion.model.SelfInvocationHit

/**
 * Finds a call to a `@Retryable`/`@CircuitBreaker`/`@Retry`/
 * `@RateLimiter`/`@Bulkhead`/`@TimeLimiter`-annotated method made from
 * another method of the *same* declaring class, unqualified or via
 * `this.` -- these annotations are all AOP-proxy-based (Spring Retry,
 * Resilience4j), so self-invocation bypasses the proxy entirely and
 * the resilience behavior is silently never applied.
 *
 * **v0.1 scope, stated honestly:** matches by simple annotation name,
 * not by resolving the real classpath symbol -- an unrelated custom
 * annotation sharing one of these names is a possible (rare) false
 * positive. Only unqualified/`this.`-qualified calls within the exact
 * same class are flagged; a call through an injected self-reference is
 * correctly never flagged, since it's qualified by a different
 * expression.
 */
object JavaSelfInvocationFinder {

    fun findAll(file: PsiFile): List<SelfInvocationHit> {
        if (file !is PsiJavaFile) return emptyList()
        val hits = mutableListOf<SelfInvocationHit>()
        file.accept(object : JavaRecursiveElementWalkingVisitor() {
            override fun visitClass(aClass: PsiClass) {
                super.visitClass(aClass)
                hits += findInClass(aClass)
            }
        })
        return hits
    }

    private fun findInClass(psiClass: PsiClass): List<SelfInvocationHit> {
        val annotatedMethodNames = mutableMapOf<String, String>()
        for (method in psiClass.methods) {
            val name = method.name
            for (annotation in method.annotations) {
                val simpleName = annotation.qualifiedName?.substringAfterLast('.') ?: continue
                if (ResilienceSignals.isResilienceAnnotationName(simpleName)) {
                    annotatedMethodNames[name] = simpleName
                    break
                }
            }
        }
        if (annotatedMethodNames.isEmpty()) return emptyList()

        val hits = mutableListOf<SelfInvocationHit>()
        for (method in psiClass.methods) {
            method.body?.accept(object : JavaRecursiveElementWalkingVisitor() {
                override fun visitMethodCallExpression(expression: PsiMethodCallExpression) {
                    super.visitMethodCallExpression(expression)
                    val name = expression.methodExpression.referenceName ?: return
                    val annotationName = annotatedMethodNames[name] ?: return

                    val qualifier = expression.methodExpression.qualifierExpression
                    val isSelfCall = qualifier == null || qualifier.text == "this"
                    if (!isSelfCall) return

                    hits += SelfInvocationHit(name, annotationName, leafOf(expression.methodExpression))
                }
            })
        }
        return hits
    }

    /** Descends to a real leaf PSI element -- LineMarkerInfo must never anchor on a composite node (SDK_GOTCHAS.md SS20). */
    private fun leafOf(element: PsiElement): PsiElement {
        var current = element
        while (current.firstChild != null) current = current.firstChild
        return current
    }
}
