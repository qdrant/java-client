package io.qdrant.client;

import io.qdrant.client.grpc.Points.Condition;
import io.qdrant.client.grpc.Points.DecayParamsExpression;
import io.qdrant.client.grpc.Points.DivExpression;
import io.qdrant.client.grpc.Points.Expression;
import io.qdrant.client.grpc.Points.GeoDistance;
import io.qdrant.client.grpc.Points.MultExpression;
import io.qdrant.client.grpc.Points.PowExpression;
import io.qdrant.client.grpc.Points.SumExpression;

/** Convenience methods for constructing {@link Expression} */
public final class ExpressionFactory {
  private ExpressionFactory() {}

  /**
   * Creates an {@link Expression} from a constant.
   *
   * @param constant The constant float value
   * @return a new instance of {@link Expression}
   */
  public static Expression constant(float constant) {
    return Expression.newBuilder().setConstant(constant).build();
  }

  /**
   * Creates an {@link Expression} from a variable name.
   *
   * @param variable The variable name (e.g., payload key or score reference)
   * @return a new instance of {@link Expression}
   */
  public static Expression variable(String variable) {
    return Expression.newBuilder().setVariable(variable).build();
  }

  /**
   * Creates an {@link Expression} from a {@link Condition}.
   *
   * @param condition The condition to evaluate
   * @return a new instance of {@link Expression}
   */
  public static Expression condition(Condition condition) {
    return Expression.newBuilder().setCondition(condition).build();
  }

  /**
   * Creates an {@link Expression} from a {@link GeoDistance}.
   *
   * @param geoDistance The geo distance object
   * @return a new instance of {@link Expression}
   */
  public static Expression geoDistance(GeoDistance geoDistance) {
    return Expression.newBuilder().setGeoDistance(geoDistance).build();
  }

  /**
   * Creates an {@link Expression} from a date-time constant string.
   *
   * @param datetime The date-time string
   * @return a new instance of {@link Expression}
   */
  public static Expression datetime(String datetime) {
    return Expression.newBuilder().setDatetime(datetime).build();
  }

  /**
   * Creates an {@link Expression} from a payload key referencing date-time values.
   *
   * @param datetimeKey The payload key containing date-time values
   * @return a new instance of {@link Expression}
   */
  public static Expression datetimeKey(String datetimeKey) {
    return Expression.newBuilder().setDatetimeKey(datetimeKey).build();
  }

  /**
   * Creates an {@link Expression} that multiplies values.
   *
   * @param mult The multiplication expression
   * @return a new instance of {@link Expression}
   */
  public static Expression mult(MultExpression mult) {
    return Expression.newBuilder().setMult(mult).build();
  }

  /**
   * Creates an {@link Expression} that sums values.
   *
   * @param sum The summation expression
   * @return a new instance of {@link Expression}
   */
  public static Expression sum(SumExpression sum) {
    return Expression.newBuilder().setSum(sum).build();
  }

  /**
   * Creates an {@link Expression} that divides values.
   *
   * @param div The division expression
   * @return a new instance of {@link Expression}
   */
  public static Expression div(DivExpression div) {
    return Expression.newBuilder().setDiv(div).build();
  }

  /**
   * Creates a negated {@link Expression}.
   *
   * @param expr The expression to negate
   * @return a new instance of {@link Expression}
   */
  public static Expression negate(Expression expr) {
    return Expression.newBuilder().setNeg(expr).build();
  }

  /**
   * Creates an {@link Expression} representing absolute value.
   *
   * @param expr The expression to wrap with abs()
   * @return a new instance of {@link Expression}
   */
  public static Expression abs(Expression expr) {
    return Expression.newBuilder().setAbs(expr).build();
  }

  /**
   * Creates an {@link Expression} representing square root.
   *
   * @param expr The expression to apply sqrt() to
   * @return a new instance of {@link Expression}
   */
  public static Expression sqrt(Expression expr) {
    return Expression.newBuilder().setSqrt(expr).build();
  }

  /**
   * Creates an {@link Expression} from a {@link PowExpression}.
   *
   * @param pow The power expression (base and exponent)
   * @return a new instance of {@link Expression}
   */
  public static Expression pow(PowExpression pow) {
    return Expression.newBuilder().setPow(pow).build();
  }

  /**
   * Creates an {@link Expression} representing exponential.
   *
   * @param expr The expression to apply exponential to
   * @return a new instance of {@link Expression}
   */
  public static Expression exp(Expression expr) {
    return Expression.newBuilder().setExp(expr).build();
  }

  /**
   * Creates an {@link Expression} representing base-10 logarithm.
   *
   * @param expr The expression to apply log10() to
   * @return a new instance of {@link Expression}
   */
  public static Expression log10(Expression expr) {
    return Expression.newBuilder().setLog10(expr).build();
  }

  /**
   * Creates an {@link Expression} representing natural logarithm.
   *
   * @param expr The expression to apply natural log to
   * @return a new instance of {@link Expression}
   */
  public static Expression ln(Expression expr) {
    return Expression.newBuilder().setLn(expr).build();
  }

  /**
   * Creates an {@link Expression} representing exponential decay.
   *
   * @param decay The decay parameters
   * @return a new instance of {@link Expression}
   */
  public static Expression expDecay(DecayParamsExpression decay) {
    return Expression.newBuilder().setExpDecay(decay).build();
  }

  /**
   * Creates an {@link Expression} representing Gaussian decay.
   *
   * @param decay The decay parameters
   * @return a new instance of {@link Expression}
   */
  public static Expression gaussDecay(DecayParamsExpression decay) {
    return Expression.newBuilder().setGaussDecay(decay).build();
  }

  /**
   * Creates an {@link Expression} representing linear decay.
   *
   * @param decay The decay parameters
   * @return a new instance of {@link Expression}
   */
  public static Expression linDecay(DecayParamsExpression decay) {
    return Expression.newBuilder().setLinDecay(decay).build();
  }
}
