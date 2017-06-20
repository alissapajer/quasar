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

package quasar.sql

import slamdata.Predef._
import quasar.fp._
import quasar.RenderTree.ops._
import quasar.specs2.QuasarMatchers._
import quasar.sql.fixpoint._

import matryoshka._
import matryoshka.data.Fix
import matryoshka.implicits._
import pathy.Path._
import scalaz._, Scalaz._

class SQLParserSpec extends quasar.Qspec {
  import SqlQueries._, ExprArbitrary._

  implicit def stringToQuery(s: String): Query = Query(s)

  def parse(query: Query): ParsingError \/ Fix[Sql] =
    fixParser.parseExpr(query).map(_.makeTables(Nil))

  "SQLParser" should {
    "parse query1" in {
      val r = parse(q1).toOption
      r should beSome
    }

    "parse query2" in {
      val r = parse(q2).toOption
      r should beSome
    }

    "parse query3" in {
      val r = parse(q3).toOption
      r should beSome
    }

    "parse query4" in {
      val r = parse(q4).toOption
      r should beSome
    }

    "parse query5" in {
      val r = parse(q5).toOption
      r should beSome
    }

    "parse query6" in {
      val r = parse(q6).toOption
      r should beSome
    }

    "parse query7" in {
      val r = parse(q7).toOption
      r should beSome
    }

    "parse query8" in {
      val r = parse(q8).toOption
      r should beSome
    }

    "parse query9" in {
      val r = parse(q9).toOption
      r should beSome
    }

    "parse query10" in {
      val r = parse(q10).toOption
      r should beSome
    }

    "parse query11" in {
      val r = parse(q11).toOption
      r should beSome
    }

    "parse query12" in {
      val r = parse(q12).toOption
      r should beSome
    }

    "parse query13" in {
      val r = parse(q13).toOption
      r should beSome
    }

    "parse query14" in {
      val r = parse(q14).toOption
      r should beSome
    }

    "parse query16" in {
      val r = parse(q16).toOption
      r should beSome
    }

    "parse query17" in {
      val r = parse(q17).toOption
      r should beSome
    }

    "parse query18" in {
      val r = parse(q18).toOption
      r should beSome
    }

    "parse query19" in {
      val r = parse(q19).toOption
      r should beSome
    }

    "parse query20" in {
      val r = parse(q20).toOption
      r should beSome
    }

    "parse query21" in {
      val r = parse(q21).toOption
      r should beSome
    }

    "parse query22" in {
      val r = parse(q22).toOption
      r should beSome
    }

    "parse basic select" in {
      parse("select foo from bar") must
        beRightDisjunction(
          SelectR(
            SelectAll,
            List(Proj(IdentR("foo"), None)),
            Some(TableRelationAST(file("bar"), None)),
            None, None, None))
    }

    "parse keywords as identifiers" in {
      parse("select as as as from from as from where where group by group order by order") should
        beRightDisjOrDiff(
          SelectR(
            SelectAll,
            List(Proj(IdentR("as"), "as".some)),
            TableRelationAST(file("from"), "from".some).some,
            IdentR("where").some,
            GroupBy(List(IdentR("group")), None).some,
            OrderBy((ASC: OrderType, IdentR("order")).wrapNel).some))
    }

    "parse ambiguous keyword as identifier" in {
      parse("""select `false` from zips""") should
        beRightDisjOrDiff(
          SelectR(
            SelectAll,
            List(Proj(IdentR("false"), None)),
            TableRelationAST(file("zips"), None).some,
            None, None, None))
    }

    "parse ambiguous expression as expression" in {
      parse("""select case from when where then and end""") should
        beRightDisjOrDiff(
          SelectR(
            SelectAll,
            List(Proj(MatchR(IdentR("from"), List(Case(IdentR("where"), IdentR("and"))), None), None)),
            None, None, None, None))
    }

    "parse partially-disambiguated expression" in {
      parse("""select `case` from when where then and end""") should
        beRightDisjOrDiff(
          SelectR(
            SelectAll,
            List(Proj(IdentR("case"), None)),
            TableRelationAST(file("when"), None).some,
            BinopR(IdentR("then"), IdentR("end"), And).some,
            None, None))
    }
    "parse quoted literal" in {
      parse("""select * from foo where bar = "abc" """).toOption should beSome
    }

    "parse quoted literal with escaped quote" in {
      parse(raw"""select * from foo where bar = "that\"s it!" """).toOption should beSome
    }

    "don’t parse multi-character char literal" in {
      parse("""select * from foo where bar = 'it!'""").toOption should beNone
    }

    "parse literal that’s too big for an Int" in {
      parse("select * from users where add_date > 1425460451000") should
        beRightDisjOrDiff(
          SelectR(
            SelectAll,
            List(Proj(SpliceR(None), None)),
            Some(TableRelationAST(file("users"),None)),
            Some(BinopR(IdentR("add_date"),IntLiteralR(1425460451000L), Gt)),
            None,None))
    }

    "parse quoted identifier" in {
      parse("""select * from `tmp/foo` """).toOption should beSome
    }

    "parse quoted identifier with escaped quote" in {
      parse(raw"select * from `tmp/foo[\`bar\`]` ").toOption should beSome
    }

    "parse simple query with two variables" in {
      parse("""SELECT * FROM zips WHERE zips.dt > :start_time AND zips.dt <= :end_time """).toOption should beSome
    }

    "parse variable with quoted name" in {
      parse(""":`start time`""") should beRightDisjOrDiff(VariR("start time"))
    }

    "parse variable with quoted name starting with '_'" in {
      parse(""":`_8`""") should beRightDisjOrDiff(VariR("_8"))
    }

    "not parse variable with '_' as the name" in {
      parse(""":_""") must
        beLeftDisjunction(
          GenericParsingError("quotedIdent expected; but found `:'"))
    }

    "not parse variable with digit as the name" in {
      parse(""":8""") must
        beLeftDisjunction(
          GenericParsingError("quotedIdent expected; but found `:'"))
    }

    "not parse variable with digit at the start of the name" in {
      parse(""":8_""") must
        beLeftDisjunction(
          GenericParsingError("quotedIdent expected; but found `:'"))
    }

    "not parse variable with '_' at the start of the name" in {
      parse(""":_8""") must
        beLeftDisjunction(
          GenericParsingError("quotedIdent expected; but found `:'"))
    }

    "parse simple query with '_' as relation" in {
      parse("""SELECT * FROM `_`""").toOption should beSome
    }

    "parse simple query with '_' in relation" in {
      parse("""SELECT * FROM `/foo/bar/_`""").toOption should beSome
    }

    "parse simple query with variable as relation" in {
      parse("""SELECT * FROM :table""").toOption should beSome
    }

    "parse true and false literals" in {
      parse("""SELECT * FROM zips WHERE zips.isNormalized = TRUE AND zips.isFruityFlavored = FALSE""").toOption should beSome
    }

    "parse “full-value” insert expression" in {
      parse("insert into zips values 1, 2, 3") should
      beRightDisjOrDiff(
        Distinct(UnionAll(
          SetLiteralR(List(IntLiteralR(1), IntLiteralR(2), IntLiteralR(3))),
          SelectR(
            SelectAll,
            List(Proj(SpliceR(None), None)),
            Some(TableRelationAST(file("zips"),None)),
            None, None, None)).embed).embed)
    }

    "parse “keyed” insert expression" in {
      parse("insert into zips ('a', 'b') values (1, 2), (3, 4)") should
      beRightDisjOrDiff(
        Distinct(UnionAll(
          SetLiteralR(List(
            MapLiteralR(List(StringLiteralR("a") -> IntLiteralR(1), StringLiteralR("b") -> IntLiteralR(2))),
            MapLiteralR(List(StringLiteralR("a") -> IntLiteralR(3), StringLiteralR("b") -> IntLiteralR(4))))),
          SelectR(
            SelectAll,
            List(Proj(SpliceR(None), None)),
            Some(TableRelationAST(file("zips"),None)),
            None, None, None)).embed).embed)
    }

    "parse numeric literals" in {
      parse("select 1, 2.0, 3000000, 2.998e8, -1.602E-19, 1e+6") should beRightDisjunction
    }

    "parse date, time, timestamp, and id literals" in {
      val q = """select * from foo
                  where dt < date("2014-11-16")
                  and tm < time("03:00:00")
                  and ts < timestamp("2014-11-16T03:00:00Z") + interval("PT1H")
                  and `_id` != oid("abc123")"""

      parse(q) must beRightDisjunction
    }

    "parse IS and IS NOT" in {
      val q = """select * from foo
                  where a IS NULL
                  and b IS NOT NULL
                  and c IS TRUE
                  and d IS NOT FALSE"""

      parse(q) must beRightDisjunction
    }

    "parse is (not) as (!)=" in {
      val q1 = "select * from zips where pop is 1000 and city is not \"BOULDER\""
      val q2 = "select * from zips where pop = 1000 and city != \"BOULDER\""
      parse(q1) must_=== parse(q2)
    }

    "parse `in` and `like` with optional `is`" in {
      val q1 = "select * from zips where pop is in (1000, 2000) and city is like \"BOU%\""
      val q2 = "select * from zips where pop in (1000, 2000) and city like \"BOU%\""
      parse(q1) must_=== parse(q2)
    }

    "parse `not in` and `not like` with optional `is`" in {
      val q1 = "select * from zips where pop is not in (1000, 2000) and city is not like \"BOU%\""
      val q2 = "select * from zips where pop not in (1000, 2000) and city not like \"BOU%\""
      parse(q1) must_=== parse(q2)
    }

    "parse nested joins left to right" in {
      val q1 = "select * from a cross join b cross join c"
      val q2 = "select * from (a cross join b) cross join c"
      parse(q1) must_=== parse(q2)
    }

    "parse nested joins with parens" in {
      val q = "select * from a cross join (b cross join c)"
      parse(q) must beRightDisjunction(
        SelectR(
          SelectAll,
          List(Proj(SpliceR(None), None)),
          Some(
            CrossRelation(
              TableRelationAST(file("a"), None),
              CrossRelation(
                TableRelationAST(file("b"), None),
                TableRelationAST(file("c"), None)))),
          None, None, None))
    }

    "parse array constructor and concat op" in {
      parse("select loc || [ pop ] from zips") must beRightDisjunction(
        SelectR(SelectAll,
          List(
            Proj(
              BinopR(IdentR("loc"),
                ArrayLiteralR(List(
                  IdentR("pop"))),
                Concat),
              None)),
          Some(TableRelationAST(file("zips"), None)),
          None, None, None))
    }

    val expectedSelect = SelectR(SelectAll,
      List(Proj(IdentR("loc"), None)),
      Some(TableRelationAST(file("places"), None)),
      None,
      None,
      None
    )
    val selectString = "select loc from places"

    "parse offset" in {
      val q = s"$selectString offset 6"
      parse(q) must beRightDisjunction(
        Offset(expectedSelect, IntLiteralR(6)).embed
      )
    }

    "parse limit" should {
      "normal" in {
        val q = s"$selectString limit 6"
        parse(q) must beRightDisjunction(
          Limit(expectedSelect, IntLiteralR(6)).embed
        )
      }
      "multiple limits" in {
        val q = s"$selectString limit 6 limit 3"
        parse(q) must beRightDisjunction(
          Limit(Limit(expectedSelect, IntLiteralR(6)).embed, IntLiteralR(3)).embed
        )
      }
      "should not allow single limit" in {
        val q = "limit 6"
        parse(q) must beLeftDisjunction
      }
    }

    "parse limit and offset" should {
      "limit before" in {
        val q = s"$selectString limit 6 offset 3"
        parse(q) must beRightDisjunction(
          Offset(Limit(expectedSelect, IntLiteralR(6)).embed, IntLiteralR(3)).embed
        )
      }
      "limit after" in {
        val q = s"$selectString offset 6 limit 3"
        parse(q) must beRightDisjunction(
          Limit(Offset(expectedSelect, IntLiteralR(6)).embed, IntLiteralR(3)).embed
        )
      }
    }

    "should refuse a semicolon not at the end" in {
      val q = "select foo from (select 5 as foo;) where foo = 7"
      parse(q) must beLeftDisjunction(
        GenericParsingError("operator ')' expected; but found `;'")
      )
    }

    "parse basic let" in {
      parse("""foo := 5; foo""") must
        beRightDisjunction(
          LetR(CIName("foo"), IntLiteralR(5), IdentR("foo")))
    }

    "parse basic let with quoted identifier starting with '_'" in {
      parse("""`_8` := 5; `_8`""") must
        beRightDisjunction(
          LetR(CIName("_8"), IntLiteralR(5), IdentR("_8")))
    }

    "not parse basic let with '_' as the identifier" in {
      parse("""_ := 5; _""") must
        beLeftDisjunction(
          GenericParsingError("quotedIdent expected; but found `*** error: '!' expected but _ found'"))
    }

    "not parse basic let with digit as the identifier" in {
      parse("""8 := 5; 8""") must
        beLeftDisjunction(
          GenericParsingError("keyword 'except' expected; but found `:='"))
    }

    "not parse basic let with digit at the start of the identifier" in {
      parse("""8_ := 5; 8_""") must
        beLeftDisjunction(
          GenericParsingError("keyword 'except' expected; but found `*** error: '!' expected but _ found'"))
    }

    "not parse basic let with '_' at the start of the identifier" in {
      parse("""_8 := 5; _8""") must
        beLeftDisjunction(
          GenericParsingError("quotedIdent expected; but found `*** error: '!' expected but _ found'"))
    }

    "parse nested lets" in {
      parse("""foo := 5; bar := "hello"; bar + foo""") must
        beRightDisjunction(
          LetR(
            CIName("foo"),
            IntLiteralR(5),
            LetR(
              CIName("bar"),
              StringLiteralR("hello"),
              BinopR(IdentR("bar"), IdentR("foo"), Plus))))
    }

    "parse let inside select" in {
      parse("""select foo from (bar := 12; baz) as quag""") must
        beRightDisjunction(
          SelectR(
            SelectAll,
            List(Proj(IdentR("foo"), None)),
            Some(ExprRelationAST(
              LetR(
                CIName("bar"),
                IntLiteralR(12),
                IdentR("baz")),
              "quag")),
            None,
            None,
            None))
    }

    "parse select inside body of let" in {
      parse("""foo := (1,2,3); select * from foo""") must
        beRightDisjunction(
          LetR(
            CIName("foo"),
            SetLiteralR(
              List(IntLiteralR(1), IntLiteralR(2), IntLiteralR(3))),
            SelectR(
              SelectAll,
              List(Proj(SpliceR(None), None)),
              Some(IdentRelationAST("foo", None)),
              None,
              None,
              None)))
    }

    "parse select inside body of let" in {
      parse("""foo := (1,2,3); select foo from bar""") must
        beRightDisjunction(
          LetR(
            CIName("foo"),
            SetLiteralR(
              List(IntLiteralR(1), IntLiteralR(2), IntLiteralR(3))),
            SelectR(
              SelectAll,
              // TODO this should be IdentRelationAST not Ident
              List(Proj(IdentR("foo"), None)),
              Some(TableRelationAST(file("bar"), None)),
              None,
              None,
              None)))
    }

    "parse select inside body of let inside select" in {
      val innerLet =
        LetR(
          CIName("foo"),
          SetLiteralR(
            List(IntLiteralR(1), IntLiteralR(2), IntLiteralR(3))),
          SelectR(
            SelectAll,
            List(Proj(SpliceR(None), None)),
            Some(IdentRelationAST("foo", None)),
            None,
            None,
            None))

      parse("""select (foo := (1,2,3); select * from foo) from baz""") must
        beRightDisjunction(
          SelectR(
            SelectAll,
            List(Proj(innerLet, None)),
            Some(TableRelationAST(file("baz"), None)),
            None,
            None,
            None))
    }

    "should parse a single-quoted character" in {
      val q = "'c'"
      parse(q) must beRightDisjunction(StringLiteralR("c"))
    }

    "should parse escaped characters" in {
      val q = raw"select '\'', '\\', '\u1234'"
      parse(q) must beRightDisjunction(
        SelectR(SelectAll, List(
          Proj(StringLiteralR("'"), None),
          Proj(StringLiteralR(raw"\"), None),
          Proj(StringLiteralR("ሴ"), None)),
          None, None, None, None))
    }
    "should parse escaped characters in a string" in {
      val q = raw""""'\\\u1234""""
      parse(q) must beRightDisjunction(StringLiteralR(raw"'\ሴ"))
    }

    "should not parse multiple expressions seperated incorrectly" in {
      val q = "select foo from bar limit 6 select biz from baz"
      parse(q) must beLeftDisjunction
    }

    "parse function declaration" in {
      val funcDeclString = "CREATE FUNCTION ARRAY_LENGTH(:foo) BEGIN COUNT(:foo[_]) END"
      fixParser.parseWithParser(funcDeclString, fixParser.func_def) must beRightDisjunction(
        FunctionDecl(CIName("ARRAY_LENGTH"),List(CIName("foo")),Fix(invokeFunction(CIName("count"),List(Fix(Unop(Fix(vari[Fix[Sql]]("foo")),ShiftArrayValues)))))))
    }

    "parse import statement" in {
      val importString = "import `/foo/bar/baz/`"
      fixParser.parseWithParser(importString, fixParser.import_) must beRightDisjunction(
        Import("/foo/bar/baz/"))
    }

    "parse module" in {
      val moduleString =
        """
          |CREATE FUNCTION ARRAY_LENGTH(:foo) BEGIN COUNT(:foo[_]) END;
          |CREATE FUNCTION USER_DATA(:user_id) BEGIN SELECT * FROM `/root/path/data/` WHERE user_id = :user_id END;
          |import `/other/stuff/in/filesystem/`
        """.stripMargin
      fixParser.parseWithParser(moduleString, fixParser.statements) must beLike {
        case \/-(List(FunctionDecl(_,_,_),FunctionDecl(_,_,_),Import(_))) => ok
      }
    }

    "parse blob" in {
      val blobString =
        """
          |CREATE FUNCTION USER_DATA(:user_id)
          |  BEGIN
          |    SELECT * FROM `/foo` WHERE user_id = :user_id
          |  END;
          |USER_DATA("bob")
        """.stripMargin
      val invokeAST: Fix[Sql] = Fix(invokeFunction[Fix[Sql]](CIName("USER_DATA"),List(Fix(stringLiteral[Fix[Sql]]("bob")))))
      fixParser.parse(blobString) must beLike {
        case \/-(Blob(`invokeAST`, List(FunctionDecl(_,_,_)))) => ok
      }
    }

    "parse array literal at top level" in {
      parse("""["X", "Y"]""") must beRightDisjunction(
        ArrayLiteralR(List(StringLiteralR("X"), StringLiteralR("Y"))))
    }

    "parse empty set literal" in {
      parse("()") must beRightDisjunction(
        SetLiteralR(Nil))
    }

    "parse parenthesized simple expression (which is syntactically identical to a 1-element set literal)" in {
      parse("(a)") must beRightDisjunction(
        IdentR("a"))
    }

    "parse 2-element set literal" in {
      parse("(a, b)") must beRightDisjunction(
        SetLiteralR(List(IdentR("a"), IdentR("b"))))
    }

    "parse deeply nested parens" in {
      // NB: Just a stress-test that the parser can handle a deeply
      // left-recursive expression with many unneeded parens, which
      // happens to be exactly what pprint produces.
      val q = """(select distinct topArr, topObj from `/demo/demo/nested` where ((((((((((((((((((((((((((((((search((((topArr)[:*])[:*])[:*], "^.*$", true)) or (search((((topArr)[:*])[:*]).a, "^.*$", true)))) or (search((((topArr)[:*])[:*]).b, "^.*$", true)))) or (search((((topArr)[:*])[:*]).c, "^.*$", true)))) or (search((((topArr)[:*]).botObj).a, "^.*$", true)))) or (search((((topArr)[:*]).botObj).b, "^.*$", true)))) or (search((((topArr)[:*]).botObj).c, "^.*$", true)))) or (search((((topArr)[:*]).botArr)[:*], "^.*$", true)))) or (search((((topObj).midArr)[:*])[:*], "^.*$", true)))) or (search((((topObj).midArr)[:*]).a, "^.*$", true)))) or (search((((topObj).midArr)[:*]).b, "^.*$", true)))) or (search((((topObj).midArr)[:*]).c, "^.*$", true)))) or (search((((topObj).midObj).botArr)[:*], "^.*$", true)))) or (search((((topObj).midObj).botObj).a, "^.*$", true)))) or (search((((topObj).midObj).botObj).b, "^.*$", true)))) or (search((((topObj).midObj).botObj).c, "^.*$", true))))"""
      parse(q).map(pprint[Fix[Sql]]) must beRightDisjunction(q)
    }

    "should not parse query with a single backslash in an identifier" should {
      "in table relation" in {
        parse(raw"select * from `\bar`") should beLeftDisjunction
      }.pendingUntilFixed("SD-1536")

      "in identifier" in {
        parse(raw"`\bar`") should beLeftDisjunction
      }.pendingUntilFixed("SD-1536")
    }

    "round-trip to SQL and back" >> prop { (node: Fix[Sql]) =>
      val parsed = parse(pprint(node))

      parsed.fold(
        _ => println(node.render.shows + "\n" + pprint(node)),
        p => if (p != node) println(pprint(p) + "\n" + (node.render diff p.render).show))

      parsed must beRightDisjOrDiff(node)
    }.set(minTestsOk = 1000) // one cannot test a parser too much

    "round-trip through the pretty-printer" >> {
      def roundTrip(q: String) = {
        val ast = parse(q)

        ast should beRightDisjunction

        ((ast ∘ ((pprint[Fix[Sql]] _) >>> (Query(_)))) >>= (parse(_))) must_=== ast
      }

      "quoted variable names" in
        roundTrip("select * from :`A.results`")

      "field deref with string literal" in
        roundTrip("select a.`_id` from z as a")

      "let binding" in
        roundTrip("a := 42; SELECT * FROM z")

      "union all" in
        roundTrip("""SELECT * FROM (SELECT 1 as v UNION ALL SELECT 2 as v) as o""")
    }
  }
}
