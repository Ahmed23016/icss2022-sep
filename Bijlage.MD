# Bijlage – Functioneel Overzicht ICSS Compiler

| Student       | Ahmed Ammoura     |
| ------------- | ----------------- |
| Project       | Compiler Opdracht |
| Studentnummer | 2130051           |
| Datum         | 31/10/2025        |
| Docent        | Dennis Breuker    |


## 1. Overzicht


De ICSS-compiler is volledig werkend opgebouwd volgens de opgegeven architectuur en de specificaties van de ICSS-20-SEP taal.
De implementatie bestaat uit de volgende componenten:

* **Parser**
* **Checker** 
* **Evaluator** 
* **Generator**
* **Eigen Stack-implementatie** 

Alle onderdelen zijn geintegreerd en getest met `level0.icss` t/m `level3.icss` en eigen tests.


## 2. Parser (40 punten)

| ID       | Omschrijving                                                                                      | Prio | Punten | Status      | Toelichting                                                |
| -------- | ------------------------------------------------------------------------------------------------- | ---- | ------ | ----------- | ---------------------------------------------------------- |
| **PA00** | De parser gebruikt een eigen implementatie van een stack generic voor `ASTNode`                   | Must | 0      | **Behaald** | `HANStack<T>` geïmplementeerd en gebruikt in `ASTListener` |
| **PA01** | Parser en listener kunnen eenvoudige opmaak verwerken (`level0.icss`)                             | Must | 10     | **Behaald** | Ondersteunt tags, id’s, classes en basale declaraties      |
| **PA02** | Parser ondersteunt variabele-assignments en referenties (`level1.icss`)                           | Must | 10     | **Behaald** | Variabelen met `:=` correct toegevoegd aan AST             |
| **PA03** | Parser ondersteunt optellen, aftrekken en vermenigvuldigen met juiste rekenregels (`level2.icss`) | Must | 10     | **Behaald** | Operatorprecedentie correct geïmplementeerd                |
| **PA04** | Parser ondersteunt if/else-statements (`level3.icss`)                                             | Must | 10     | **Behaald** | Nested if/else wordt volledig ondersteund                  |
| **PA05** | PA01 t/m PA04 leveren minimaal 30 punten op                                                       | Must | 0      | **Behaald** | Volledige parser-functionaliteit aanwezig                  |

**Totaal Parser:** 40 punten


## 3. Checker (30 punten)

| ID       | Omschrijving                                     | Prio   | Punten | Status      | Toelichting                                      |
| -------- | ------------------------------------------------ | ------ | ------ | ----------- | ------------------------------------------------ |
| **CH00** | Minimaal vier checks geïmplementeerd             | Must   | 0      | **Behaald** | Alle zes checks geïmplementeerd                  |
| **CH01** | Controle op ongedefinieerde variabelen           | Should | 5      | **Behaald** | Variabelen gecontroleerd via scope-stack         |
| **CH02** | Controle op operandtypes bij `+`, `-`, `*`       | Should | 5      | **Behaald** | Pixel, percentage en scalar correct gevalideerd  |
| **CH03** | Controle dat kleuren niet in operaties voorkomen | Should | 5      | **Behaald** | Kleur-literals geblokkeerd in expressies         |
| **CH04** | Controle op juiste property-type                 | Should | 5      | **Behaald** | Mapping tussen property en toegestaan type       |
| **CH05** | Controle of if-condities boolean zijn            | Should | 5      | **Behaald** | Controle op `BoolLiteral` of `VariableReference` |
| **CH06** | Controle op correcte scopes van variabelen       | Must   | 5      | **Behaald** | Gerealiseerd met nested `HashMap`-stack          |

**Totaal Checker:** 30 punten


## 4. Evaluator (20 punten)

| ID       | Omschrijving                                                       | Prio | Punten | Status      | Toelichting                                          |
| -------- | ------------------------------------------------------------------ | ---- | ------ | ----------- | ---------------------------------------------------- |
| **TR01** | Evalueer expressies: vervang `Expression` door berekende `Literal` | Must | 10     | **Behaald** | Ondersteunt `+`, `-`, `*` en juiste types            |
| **TR02** | Evalueer if/else: verwijder `IfClause`, behoud geldige body        | Must | 10     | **Behaald** | If/else volledig opgelost in AST, nested ondersteund |

**Totaal Evaluator:** 20 punten


## 5. Generator (10 punten)

| ID       | Omschrijving                                    | Prio | Punten | Status      | Toelichting                                 |
| -------- | ----------------------------------------------- | ---- | ------ | ----------- | ------------------------------------------- |
| **GE01** | Generator zet AST om naar CSS2-compliant string | Must | 5      | **Behaald** | Correcte CSS2-structuur met alle properties |
| **GE02** | Twee spaties inspringing per scopeniveau        | Must | 5      | **Behaald** | Indentatie consistent toegepast             |

**Totaal Generator:** 10 punten


## 6. Eigen uitbreiding (0 punten)

Geen uitbreiding geïmplementeerd.


## 7. Eindresultaat

| Categorie | ID’s behaald | Punten                   |
| ------- |--------------|--------------------------|
| Parser  | PA00–PA04    | 40                       |
| Checker | CH00–CH06    | 30                       |
| Evaluator | TR01–TR02    | 20                       |
| Generator | GE01–GE02    | 10                       |
| Uitbreiding | Geen         | 0                        |
| Totaal  | Geen         | 100 / 120 volgens mij :) |

