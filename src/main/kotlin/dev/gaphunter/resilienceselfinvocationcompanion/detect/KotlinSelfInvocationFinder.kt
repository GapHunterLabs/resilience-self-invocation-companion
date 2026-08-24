package dev.gaphunter.resilienceselfinvocationcompanion.detect

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import dev.gaphunter.resilienceselfinvocationcompanion.model.SelfInvocationHit
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid

/** Kotlin counterpart of [JavaSelfInvocationFinder]. */
object KotlinSelfInvocationFinder {

    fun findAll(file: PsiFile): List<SelfInvocationHit> {
        if (file !is KtFile) return emptyList()
        val hits = mutableListOf<SelfInvocationHit>()
        file.accept(object : KtTreeVisitorVoid() {
            override fun visitClass(klass: KtClass) {
                super.visitClass(klass)
                hits += findInClass(klass)
            }
        })
        return hits
    }

    private fun findInClass(klass: KtClass): List<SelfInvocationHit> {
        val functions = klass.declarations.filterIsInstance<KtNamedFunction>()
        val annotatedMethodNames = mutableMapOf<String, String>()
        for (function in functions) {
            val name = function.name ?: continue
            for (entry in function.annotationEntries) {
                val simpleName = entry.shortName?.asString() ?: continue
                if (ResilienceSignals.isResilienceAnnotationName(simpleName)) {
                    annotatedMethodNames[name] = simpleName
                    break
                }
            }
        }
        if (annotatedMethodNames.isEmpty()) return emptyList()

        val hits = mutableListOf<SelfInvocationHit>()
        for (function in functions) {
            function.bodyExpression?.accept(object : KtTreeVisitorVoid() {
                override fun visitCallExpression(expression: KtCallExpression) {
                    super.visitCallExpression(expression)
                    val name = expression.calleeExpression?.text ?: return
                    val annotationName = annotatedMethodNames[name] ?: return

                    val immediateParent = expression.parent as? KtDotQualifiedExpression
                    val isDirectlyQualified = immediateParent != null && immediateParent.selectorExpression === expression
                    val receiverText = if (isDirectlyQualified) immediateParent?.receiverExpression?.text else null
                    val isSelfCall = !isDirectlyQualified || receiverText == "this"
                    if (!isSelfCall) return

                    hits += SelfInvocationHit(name, annotationName, leafOf(expression.calleeExpression!!))
                }
            })
        }
        return hits
    }

    private fun leafOf(element: PsiElement): PsiElement {
        var current = element
        while (current.firstChild != null) current = current.firstChild
        return current
    }
}
