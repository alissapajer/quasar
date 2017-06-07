/*
 * Copyright 2014–2017 SlamData Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package quasar.physical.marklogic.qscript

import slamdata.Predef._
import quasar.Data
import quasar.contrib.scalaz.eitherT._
import quasar.physical.marklogic.DocType
import quasar.physical.marklogic.xquery._
import quasar.physical.marklogic.xquery.syntax._

import matryoshka._
import scalaz._, Scalaz._
import xml.name._

final class XmlStructuralPlannerSpec
  extends StructuralPlannerSpec[XmlStructuralPlannerSpec.XmlPlan, DocType.Xml] {

  import XmlStructuralPlannerSpec.XmlPlan
  import expr._

  val SP  = StructuralPlanner[XmlPlan, DocType.Xml]
  val DP  = Planner[XmlPlan, DocType.Xml, Const[Data, ?]]
  val toM = λ[XmlPlan ~> M](xp => EitherT(WriterT.writer(xp.leftMap(_.shows.wrapNel).run.run.eval(1))))
  def asMapKey(qn: QName) = qn.xqy.point[XmlPlan]

  xquerySpec(_ => "XML Specific") { evalM =>
    val eval = evalM.compose[XmlPlan[XQuery]](toM(_))

    "arrayElementAt" >> {
      "returns the nth entry of an object" >> prop { (a: Data, b: Data, c: Data, d: Data) =>
        val es = Data._obj(ListMap(keyed(NonEmptyList(a, b, c, d)).toList: _*))
        eval(lit(es) >>= (SP.arrayElementAt(_, 2.xqy))) must resultIn(c)
      }
    }

    "nodeMetadata" >> {
      "returns element attributes as an object" >> {
        val book = element("book".xs)(mkSeq_(
          attribute("author".xs)("Ursula K. LeGuin".xs),
          attribute("published".xs)(1972.xqy),
          "The Farthest Shore".xs
        ))

        eval(SP.nodeMetadata(book)) must resultIn(Data.Obj(
          "author"    -> Data._str("Ursula K. LeGuin"),
          "published" -> Data._str("1972")
        ))
      }

      "returns attributes of an empty element" >> {
        val person = element("person".xs)(attribute("name".xs)("Alice".xs))
        eval(SP.nodeMetadata(person)) must resultIn(Data.Obj(
          "name" -> Data._str("Alice")
        ))
      }

      "returns an empty object when element has no attributes" >> {
        val foo = element("foo".xs)("bar".xs)
        eval(SP.nodeMetadata(foo)) must resultIn(Data.Obj())
      }

      "returns an empty object when empty element has no attributes" >> {
        val baz = element("baz".xs)(emptySeq)
        eval(SP.nodeMetadata(baz)) must resultIn(Data.Obj())
      }
    }

    "objectDelete" >> {
      "deletes non-QName keys" >> prop { (x: Data, y: Data) =>
        val obj = (lit(x) |@| lit(y))((a, b) => for {
          e1 <- SP.mkObjectEntry(xs.QName("bar".xs), a)
          e2 <- SP.mkObjectEntry("12 not qname".xs, b)
          o  <- SP.mkObject(mkSeq_(e1, e2))
        } yield o).join

        eval(obj.flatMap(SP.objectDelete(_, "12 not qname".xs))) must resultIn(Data.Obj("bar" -> x))
      }
    }

    "objectLookup" >> {
      "returns repeated elements as an array" >> prop { (x: Data, y: Data, z: Data) =>
        val obj = (lit(x) |@| lit(y) |@| lit(z))((a, b, c) => for {
                    e1 <- SP.mkObjectEntry(xs.QName("bar".xs), a)
                    e2 <- SP.mkObjectEntry(xs.QName("baz".xs), b)
                    e3 <- SP.mkObjectEntry(xs.QName("baz".xs), c)
                    o  <- SP.mkObject(mkSeq_(e1, e2, e3))
                  } yield o).join

        eval(obj >>= (SP.objectLookup(_, xs.QName("baz".xs)))) must resultIn(Data._arr(List(y, z)))
      }

      "returns non-QName keys" >> prop { x: Data =>
        val obj = lit(x).flatMap(a     => SP.mkObjectEntry("12 not qname".xs, a))
                        .flatMap(entry => SP.mkObject(mkSeq_(entry)))

        eval(obj >>= (SP.objectLookup(_, "12 not qname".xs))) must resultIn (x)
      }
    }
  }
}

object XmlStructuralPlannerSpec {
  type XmlPlan[A] = MarkLogicPlanErrT[PrologT[State[Long, ?], ?], A]
}
