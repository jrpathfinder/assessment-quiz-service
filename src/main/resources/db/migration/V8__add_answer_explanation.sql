-- V8: Per-answer explanations (like ITBelts — each option explains why it is right or wrong)
ALTER TABLE possible_answer ADD COLUMN IF NOT EXISTS explanation TEXT;

-- ── Java Core, Q1: NOT a primitive type ─────────────────────────────────────
UPDATE possible_answer SET explanation = 'int IS one of Java''s 8 primitive types.'
 WHERE question_id = (SELECT id FROM question WHERE question_text LIKE '%NOT a primitive%') AND text = 'int';
UPDATE possible_answer SET explanation = 'boolean IS one of Java''s 8 primitive types.'
 WHERE question_id = (SELECT id FROM question WHERE question_text LIKE '%NOT a primitive%') AND text = 'boolean';
UPDATE possible_answer SET explanation = 'String is a class (reference type) that lives on the heap — not a primitive.'
 WHERE question_id = (SELECT id FROM question WHERE question_text LIKE '%NOT a primitive%') AND text = 'String';
UPDATE possible_answer SET explanation = 'char IS a primitive type, representing a single 16-bit Unicode character.'
 WHERE question_id = (SELECT id FROM question WHERE question_text LIKE '%NOT a primitive%') AND text = 'char';

-- ── Q2: 10 / 3 ───────────────────────────────────────────────────────────────
UPDATE possible_answer SET explanation = 'Only if one operand were a float or double would you get a decimal result.'
 WHERE question_id = (SELECT id FROM question WHERE question_text LIKE '%10 / 3%') AND text = '3.33';
UPDATE possible_answer SET explanation = 'Both operands are int → integer division truncates the decimal. 10 ÷ 3 = 3 remainder 1.'
 WHERE question_id = (SELECT id FROM question WHERE question_text LIKE '%10 / 3%') AND text = '3';
UPDATE possible_answer SET explanation = 'Integer division truncates toward zero — it does not round up.'
 WHERE question_id = (SELECT id FROM question WHERE question_text LIKE '%10 / 3%') AND text = '4';
UPDATE possible_answer SET explanation = '3.0 would require at least one floating-point operand (e.g. 10.0 / 3).'
 WHERE question_id = (SELECT id FROM question WHERE question_text LIKE '%10 / 3%') AND text = '3.0';

-- ── Q3: prevent subclassing ──────────────────────────────────────────────────
UPDATE possible_answer SET explanation = 'static makes a member belong to the class rather than an instance — it has no effect on inheritance.'
 WHERE question_id = (SELECT id FROM question WHERE question_text LIKE '%prevent a class from being subclassed%') AND text = 'static';
UPDATE possible_answer SET explanation = 'abstract is the opposite: it forces at least one subclass to exist to provide the implementation.'
 WHERE question_id = (SELECT id FROM question WHERE question_text LIKE '%prevent a class from being subclassed%') AND text = 'abstract';
UPDATE possible_answer SET explanation = 'final on a class forbids any subclass. Example: public final class String {}.'
 WHERE question_id = (SELECT id FROM question WHERE question_text LIKE '%prevent a class from being subclassed%') AND text = 'final';
UPDATE possible_answer SET explanation = 'sealed (Java 17+) restricts which named classes may extend it, but does not prevent all subclassing.'
 WHERE question_id = (SELECT id FROM question WHERE question_text LIKE '%prevent a class from being subclassed%') AND text = 'sealed';

-- ── Q4: insertion order + duplicates ─────────────────────────────────────────
UPDATE possible_answer SET explanation = 'HashSet is unordered and disallows duplicate elements.'
 WHERE question_id = (SELECT id FROM question WHERE question_text LIKE '%insertion order%duplicates%') AND text = 'HashSet';
UPDATE possible_answer SET explanation = 'TreeSet keeps elements sorted (not insertion order) and does not allow duplicates.'
 WHERE question_id = (SELECT id FROM question WHERE question_text LIKE '%insertion order%duplicates%') AND text = 'TreeSet';
UPDATE possible_answer SET explanation = 'ArrayList implements List: it preserves insertion order and allows any number of duplicate elements.'
 WHERE question_id = (SELECT id FROM question WHERE question_text LIKE '%insertion order%duplicates%') AND text = 'ArrayList';
UPDATE possible_answer SET explanation = 'HashMap is a key→value store; keys are unique and it has no concept of element order or duplicates.'
 WHERE question_id = (SELECT id FROM question WHERE question_text LIKE '%insertion order%duplicates%') AND text = 'HashMap';

-- ── Q5: static method ────────────────────────────────────────────────────────
UPDATE possible_answer SET explanation = 'Static methods can be hidden by a subclass, but that is method hiding — not overriding.'
 WHERE question_id = (SELECT id FROM question WHERE question_text LIKE '%static%method%') AND text LIKE '%cannot be overridden%';
UPDATE possible_answer SET explanation = 'Correct. Static methods belong to the class and are invoked as ClassName.method() without an instance.'
 WHERE question_id = (SELECT id FROM question WHERE question_text LIKE '%static%method%') AND text LIKE '%belongs to the class%';
UPDATE possible_answer SET explanation = 'static has no relation to threading. Thread creation uses Thread or Runnable.'
 WHERE question_id = (SELECT id FROM question WHERE question_text LIKE '%static%method%') AND text LIKE '%separate thread%';
UPDATE possible_answer SET explanation = 'Package-private access is achieved by omitting any modifier, not by using static.'
 WHERE question_id = (SELECT id FROM question WHERE question_text LIKE '%static%method%') AND text LIKE '%within the package%';

-- ── Q6: checked exceptions ───────────────────────────────────────────────────
UPDATE possible_answer SET explanation = 'NullPointerException extends RuntimeException — it is unchecked; the compiler does not require you to handle it.'
 WHERE question_id = (SELECT id FROM question WHERE question_text LIKE '%checked exceptions%') AND text = 'NullPointerException';
UPDATE possible_answer SET explanation = 'IOException extends Exception (not RuntimeException) — it is checked; you must catch or declare it.'
 WHERE question_id = (SELECT id FROM question WHERE question_text LIKE '%checked exceptions%') AND text = 'IOException';
UPDATE possible_answer SET explanation = 'ArrayIndexOutOfBoundsException extends RuntimeException — unchecked.'
 WHERE question_id = (SELECT id FROM question WHERE question_text LIKE '%checked exceptions%') AND text = 'ArrayIndexOutOfBoundsException';
UPDATE possible_answer SET explanation = 'SQLException extends Exception — it is checked; JDBC code must always handle it explicitly.'
 WHERE question_id = (SELECT id FROM question WHERE question_text LIKE '%checked exceptions%') AND text = 'SQLException';

-- ── Q7: default value of int ─────────────────────────────────────────────────
UPDATE possible_answer SET explanation = 'null is the default for reference types (objects), not for primitives.'
 WHERE question_id = (SELECT id FROM question WHERE question_text LIKE '%default value of an int%') AND text = 'null';
UPDATE possible_answer SET explanation = 'All numeric primitive fields are initialized to zero by the JVM: int → 0, double → 0.0, etc.'
 WHERE question_id = (SELECT id FROM question WHERE question_text LIKE '%default value of an int%') AND text = '0';
UPDATE possible_answer SET explanation = 'No primitive type defaults to -1 in Java.'
 WHERE question_id = (SELECT id FROM question WHERE question_text LIKE '%default value of an int%') AND text = '-1';
UPDATE possible_answer SET explanation = 'undefined is a JavaScript concept; Java primitives always have a defined default value.'
 WHERE question_id = (SELECT id FROM question WHERE question_text LIKE '%default value of an int%') AND text = 'undefined';

-- ── Q8: == vs .equals() ──────────────────────────────────────────────────────
UPDATE possible_answer SET explanation = 'They only coincide when the JVM interns strings (string pool), which is not guaranteed for all String objects.'
 WHERE question_id = (SELECT id FROM question WHERE question_text LIKE '%==%equals%') AND text LIKE '%identical%';
UPDATE possible_answer SET explanation = '== checks if both references point to the same object in memory. .equals() compares character-by-character content.'
 WHERE question_id = (SELECT id FROM question WHERE question_text LIKE '%==%equals%') AND text LIKE '%== compares references%';
UPDATE possible_answer SET explanation = 'This is the opposite of the truth. .equals() always compares content for String; == compares references.'
 WHERE question_id = (SELECT id FROM question WHERE question_text LIKE '%==%equals%') AND text LIKE '%.equals() compares references%';
UPDATE possible_answer SET explanation = '== is faster but unreliable for content comparison. Always use .equals() to compare String values.'
 WHERE question_id = (SELECT id FROM question WHERE question_text LIKE '%==%equals%') AND text LIKE '%faster%';
