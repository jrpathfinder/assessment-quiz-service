-- Java Core exam under the Java category (id=1)
INSERT INTO exam (name, description, category_id, max_time_minutes, success_percentage, status)
VALUES ('Java Core', 'Fundamentals: JVM, types, OOP, Collections, exceptions and basic concurrency', 1, 20, 70, 'RELEASED');

-- ── Questions ────────────────────────────────────────────────────────────────
-- Q1
INSERT INTO question (exam_id, question_text, explanation, status, answer_type)
VALUES (1, 'Which of the following is NOT a primitive type in Java?',
        'Java has 8 primitive types: byte, short, int, long, float, double, boolean, char. String is an object (reference type).',
        'RELEASED', 'RADIO');

INSERT INTO possible_answer (question_id, text, is_correct, order_index) VALUES
  (1, 'int',     false, 0),
  (1, 'boolean', false, 1),
  (1, 'String',  true,  2),
  (1, 'char',    false, 3);

-- Q2
INSERT INTO question (exam_id, question_text, explanation, status, answer_type)
VALUES (1, 'What is the output of: System.out.println(10 / 3);',
        'Both operands are integers, so Java performs integer division and truncates the result. 10 / 3 = 3 (remainder discarded).',
        'RELEASED', 'RADIO');

INSERT INTO possible_answer (question_id, text, is_correct, order_index) VALUES
  (2, '3.33', false, 0),
  (2, '3',    true,  1),
  (2, '4',    false, 2),
  (2, '3.0',  false, 3);

-- Q3
INSERT INTO question (exam_id, question_text, explanation, status, answer_type)
VALUES (1, 'Which keyword is used to prevent a class from being subclassed?',
        'The "final" keyword applied to a class prevents inheritance. e.g. public final class String {}',
        'RELEASED', 'RADIO');

INSERT INTO possible_answer (question_id, text, is_correct, order_index) VALUES
  (3, 'static',   false, 0),
  (3, 'abstract', false, 1),
  (3, 'final',    true,  2),
  (3, 'sealed',   false, 3);

-- Q4
INSERT INTO question (exam_id, question_text, explanation, status, answer_type)
VALUES (1, 'Which collection maintains insertion order and allows duplicates?',
        'ArrayList is a List implementation that preserves insertion order and allows duplicate elements. HashSet does not maintain order and does not allow duplicates.',
        'RELEASED', 'RADIO');

INSERT INTO possible_answer (question_id, text, is_correct, order_index) VALUES
  (4, 'HashSet',    false, 0),
  (4, 'TreeSet',    false, 1),
  (4, 'ArrayList',  true,  2),
  (4, 'HashMap',    false, 3);

-- Q5
INSERT INTO question (exam_id, question_text, explanation, status, answer_type)
VALUES (1, 'What does the "static" keyword mean when applied to a method?',
        'A static method belongs to the class itself, not to any instance. It can be called without creating an object: ClassName.method().',
        'RELEASED', 'RADIO');

INSERT INTO possible_answer (question_id, text, is_correct, order_index) VALUES
  (5, 'The method cannot be overridden',                    false, 0),
  (5, 'The method belongs to the class, not an instance',   true,  1),
  (5, 'The method runs in a separate thread',               false, 2),
  (5, 'The method is only accessible within the package',   false, 3);

-- Q6
INSERT INTO question (exam_id, question_text, explanation, status, answer_type)
VALUES (1, 'Which of the following are checked exceptions? (select all that apply)',
        'IOException and SQLException are checked exceptions — the compiler forces you to handle them. NullPointerException and ArrayIndexOutOfBoundsException are unchecked (RuntimeException subclasses).',
        'RELEASED', 'CHECKBOX');

INSERT INTO possible_answer (question_id, text, is_correct, order_index) VALUES
  (6, 'NullPointerException',          false, 0),
  (6, 'IOException',                   true,  1),
  (6, 'ArrayIndexOutOfBoundsException',false, 2),
  (6, 'SQLException',                  true,  3);

-- Q7
INSERT INTO question (exam_id, question_text, explanation, status, answer_type)
VALUES (1, 'What is the default value of an int field in a Java class?',
        'Instance fields of primitive types are automatically initialized: int/long/short/byte = 0, double/float = 0.0, boolean = false, char = ''\u0000''.',
        'RELEASED', 'RADIO');

INSERT INTO possible_answer (question_id, text, is_correct, order_index) VALUES
  (7, 'null',  false, 0),
  (7, '0',     true,  1),
  (7, '-1',    false, 2),
  (7, 'undefined', false, 3);

-- Q8
INSERT INTO question (exam_id, question_text, explanation, status, answer_type)
VALUES (1, 'What is the difference between == and .equals() for String comparison?',
        '== compares object references (memory addresses). .equals() compares the actual character content. Two String literals may share the same pool reference, but new String("x") always creates a new object.',
        'RELEASED', 'RADIO');

INSERT INTO possible_answer (question_id, text, is_correct, order_index) VALUES
  (8, 'They are identical for Strings',                                         false, 0),
  (8, '== compares references; .equals() compares content',                     true,  1),
  (8, '.equals() compares references; == compares content',                     false, 2),
  (8, '== is faster and always preferred',                                      false, 3);
