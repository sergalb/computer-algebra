import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.math.abs

class DixonTests {
    private fun shouldFactorized(n: Long, factor: Long?) = (factor != null && abs(factor) != 1L && n % factor == 0L)

    @Test
    fun `small size number should be factorized`() {
        val res = factorize(89755L)
        Assertions.assertTrue(shouldFactorized(89755, res))
    }

    @Test
    fun `midle size number should be factorized`() {
        val res = factorize(37 * 59 * 61 * 97L)
        Assertions.assertTrue(shouldFactorized(37 * 59 * 61 * 97L, res))
    }

    @Test
    fun `big size number should be factorized`() {
        val res = factorize(1125899839733757L)
        Assertions.assertTrue(shouldFactorized(1125899839733757L, res))
    }

    @Test
    fun `composite number from three primes`() {
        val res = factorize(141 * 223 * 16127L)
        Assertions.assertTrue(shouldFactorized(141 * 223 * 16127L, res))
    }

    @Test
    fun `16769021 should factorize`() {
        val res = factorize(16769021)
        Assertions.assertTrue(shouldFactorized(16769021, res))
    }

    @Test
    fun `prime shouldn't be factorized`() {
        val res = factorize(17)
        Assertions.assertNull(res)
    }

    @Test
    fun `big prime shouldn't be factorized`() {
        val res = factorize(1125899839733759)
        Assertions.assertNull(res)
    }

}