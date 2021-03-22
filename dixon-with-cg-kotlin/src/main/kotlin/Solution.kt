import kotlin.math.*
import kotlin.random.Random.Default.nextLong

fun generatePrimes(n: Long): List<Long> {
    val prime = Array(n.toInt() + 1) { true }
    var p = 2
    while (p * p <= n) {
        if (prime[p]) {
            var i = p * p
            while (i <= n) {
                prime[i] = false
                i += p
            }
        }
        p++
    }
    return (2..n).filter { prime[it.toInt()] }
}

fun generateBSmooth(
    h: Int,
    n: Long,
    primes: List<Long>
): Triple<MutableList<Long>, Array<Array<Int>>, Array<Array<Long>>> {
    val bSmooth: MutableList<Long> = ArrayList(h + 1)
    val alphas = Array(h + 1) { Array(h) { 0 } }
    val epsilons = Array(h + 1) { Array(primes.size) { 0L } }
    var countBSmooth = 0
    while (countBSmooth < h + 1) {
        val candidate = nextLong(sqrt(n.toDouble()).toLong(), n)
        var a = (candidate * candidate) % n
        val alpha = Array(h) { 0 }
        for (i in primes.indices) {
            val prime = primes[i]
            var power = 0
            while (a % prime == 0L) {
                power++
                a /= prime
            }
            alpha[i] = power
            epsilons[countBSmooth][i] = (power % 2).toLong()
        }
        if (a == 1L) {
            alphas[countBSmooth] = alpha
            countBSmooth++
            bSmooth.add(candidate)
        }

    }
    return Triple(bSmooth, alphas, epsilons)
}

fun L(n: Double) = sqrt(exp(sqrt(ln(n) * ln(ln(n)))))

private fun gcd(a: Long, b: Long): Long {
    if (a == 0L) return b
    return gcd(b % a, a)
}


fun factorize(n: Long): Long? {
    val m = L(n.toDouble())
    val primes = generatePrimes(m.toLong())
    val h = primes.size
    var factor: Long? = null
    for (attempt in 0..10) {
        val t = generateBSmooth(h, n, primes)
        val bSmooth = t.first
        val alphas = t.second
        val epsilons = t.third
        val matrix = SparseMatrix(2 shl 20, epsilons)
        matrix.inPlaceTranspose()
        try {
            val solutionAttempt = matrix.conjugateGradientMethod()
            if (solutionAttempt.isPresent) {
                val x = solutionAttempt.get().map { it % 2 }
                var left = (1L).toBigInteger()
                val gamma = Array(primes.size) { 0 }
                for (i in x.indices) {
                    if (x[i] != 0L) {
                        left = left * bSmooth[i].toBigInteger() % n.toBigInteger()
                        for (j in primes.indices) {
                            gamma[j] += alphas[i][j]
                        }
                    }
                }
                var right = (1L).toBigInteger()
                for (i in primes.indices) {
                    gamma[i] /= 2
                    right = right * primes[i].toDouble().pow(gamma[i]).toLong()
                        .toBigInteger() % n.toBigInteger()
                }
                val sumGcd = abs(gcd((left + right).toLong(), n))
                val subGcd = abs(gcd(abs((left - right).toLong()), n))
                if (left != right && left != n.toBigInteger() - right && (sumGcd != 1L || subGcd != 1L)) {
                    println("s, t from Dixon algorithm: $left, $right")
                    if (abs(sumGcd) != 1L) {
                        factor = sumGcd
                    } else {
                        factor = subGcd
                    }
                    break
                }
            }
        } catch (ignore: RuntimeException) {
        }
        println("attempt ${attempt + 1} for factorizing failed")

    }
    return factor


}

fun main() {
    val n = 1125899839733757L
    val factor = factorize(n)
    if (factor != null) {
        println("factor: ${factor}")
    } else {
        println("couldn't factorize, may be input is prime?")

    }
}
