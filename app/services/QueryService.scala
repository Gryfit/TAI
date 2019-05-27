package services

import akka.stream.scaladsl.Sink
import javax.inject.Inject
import models._
import org.joda.time.DateTime
import repositories.UserRepository
import sangria.schema.{StringType, _}
import services.impl.NBPCurrencyService
import cats.implicits._

import scala.concurrent.Future

class QueryService @Inject() (
  val currencyRateRegressionService: CurrencyRateRegressionService,
  val nBPCurrencyService: NBPCurrencyService,
  val profitEstimationService: ProfitEstimationService,
  val userRepository: UserRepository
) {}

object QueryService {

  import Actors._

  val QueryType = ObjectType("Query", fields[QueryService, Unit](
    Field("currencies", ListType(Currency.CurrencyType),
      description = Some("Returns list of all currencies"),
      resolve = _.ctx.nBPCurrencyService.fetchCurrencyList().runWith(Sink.seq).map(_.toList)
    ),

    Field("exchanges", ListType(LatestExchangeRates.LatestExchangeRatesType),
      description = Some("Returns latest exchange rates"),
      resolve = _.ctx.nBPCurrencyService.fetchLatestExchangeRates().runWith(Sink.seq).map(_.toList)
    ),

    Field("currencyFromPeriod", CurrencyPeriodData.CurrencyPeriodDataType,
      description = Some("Returns currency given currency rates from period"),
      arguments = List(Argument("code", StringType), Argument("from", DateConverter.DateTimeType), Argument("to", DateConverter.DateTimeType)),
      resolve = e => {

        val currency: Currency = Currency("", e.arg("code"))
        val from: DateTime = e.arg("from")
        val to: DateTime = e.arg("to")

        // Not the best but for some reason scala cant figure out the correct type
        val x: Future[CurrencyPeriodData] = e.ctx.nBPCurrencyService.fetchCurrencyDataFromPeriod(
          CurrencyPeriod(currency, from, to)
        ).runWith(Sink.head)

        x
      }),

    Field("currencyFromPeriodTrend", CurrencyPeriodData.CurrencyPeriodDataType,
      description = Some("Returns calculated trend (linear regresion) for currency exchange rate from period"),
      arguments = List(Argument("code", StringType), Argument("from", DateConverter.DateTimeType), Argument("to", DateConverter.DateTimeType)),
      resolve = e => {
        val currency: Currency = Currency("", e.arg("code"))
        val from: DateTime = e.arg("from")
        val to: DateTime = e.arg("to")
        val x: Future[CurrencyPeriodData] = e.ctx.nBPCurrencyService.fetchCurrencyDataFromPeriod(
          CurrencyPeriod(currency, from, to)
        ).map(e.ctx.currencyRateRegressionService.calculateRegression)
          .runWith(Sink.head)
        x
      }
    ),
    Field("estimateProfit", ProfitEstimationResponse.ProfitEstimationResponseDataType,
      description = Some("Returns estimated profit from a given period of time"),
      arguments = List(
        Argument("code", StringType),
        Argument("from", DateConverter.DateTimeType),
        Argument("to", DateConverter.DateTimeType),
        Argument("tax", IntType),
        Argument("amount", IntType)
      ),
      resolve = e => {
        val request = ProfitEstimationRequest(
          e.arg[String]("code"),
          e.arg[DateTime]("from"),
          e.arg[DateTime]("to"),
          e.arg[Int]("tax"),
          e.arg[Int]("amount")
        )

        val x: Future[ProfitEstimationResponse] = e.ctx.profitEstimationService.calculateProfit(request).runWith(Sink.head)
        x
      }),

      Field("findClient", OptionType(User.UserDataType),
        description = Some("client data for dashboard"),
        arguments = List(
          Argument("email", StringType)
        ),
        resolve = e => {
          e.ctx.userRepository.find(e.arg[String]("email")).map(_.headOption)
        }
      )
  ))

  val MutationType = ObjectType("Mutation", fields[QueryService, Unit](
    Field("saveUser", StringType,
      description = Some("Save user"),
      arguments = List(
        Argument("firstName", StringType),
        Argument("lastName", StringType),
        Argument("email", StringType),
        Argument("dashboardCurrencies", ListInputType(StringType))
      ),
      resolve = e => {
        e.ctx.userRepository.save(
          User(
            e.arg[String]("firstName"),
            e.arg[String]("lastName"),
            e.arg[String]("email"),
            e.arg[Vector[String]]("dashboardCurrencies").toList,
          )
        )
      e.arg[String]("email")
      }
    )
  ))


  val schema = Schema(QueryType, MutationType.some)
}
