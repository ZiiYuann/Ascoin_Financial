package com.tianli.tool.judge;


import org.javatuples.Pair;
import org.javatuples.Triplet;

import java.util.LinkedList;
import java.util.function.LongPredicate;

/**
 * @Author wangqiyun
 * @Date 2019/7/16 13:48
 */
public class JudgeText {

    public boolean test() {
        return judge(e -> true, true) != null;
    }

    public Boolean judge(LongPredicate judge, boolean default_result) {
        index = 0;
        result.clear();
        brackets.clear();
        action.clear();
        Triplet<LexicalType, Long, Character> lexical = next();
        boolean negate = false;
        while (!LexicalType.end.equals(lexical.getValue0())) {
            switch (lexical.getValue0()) {
                case bool:
                    if (result.size() != action.size()) return null;
                    result.addLast(new LongBool(lexical.getValue1() != 0L).setNegate(negate));
                    negate = false;
                    break;
                case digit:
                    if (result.size() != action.size()) return null;
                    result.addLast(new LongBool(lexical.getValue1()).setNegate(negate));
                    negate = false;
                    break;
                case character:
                    if (result.size() != action.size() + 1) return null;
                    action.addLast(lexical.getValue2());
                    if (negate) return null;
                    break;
                case brackets:
                    if (lexical.getValue2() == '(') {
                        if (result.size() != action.size()) return null;
                        brackets.addLast(Pair.with(result.size(), negate));
                        negate = false;
                    } else {
                        if (negate) return null;
                        Pair<Integer, Boolean> end = brackets.pollLast();
                        if (end == null) return null;
                        boolean calc = calc(end, judge);
                        if (!calc) return null;
                    }
                    break;
                case negate:
                    negate = !negate;
                    break;
                case error:
                    return null;
            }
            lexical = next();
        }
        if (negate) return null;
        if (!brackets.isEmpty()) return null;
        if (result.isEmpty() && action.isEmpty()) return default_result;
        if (result.size() != action.size() + 1) return null;
        if (!calc(Pair.with(0, false), judge)) return null;
        return result.removeLast().get(judge);
    }


    private Triplet<LexicalType, Long, Character> next() {
        if (index < text.length) {
            char c = text[index];
            if (Character.isDigit(c) || c == '-') {
                StringBuilder str = new StringBuilder();
                str.append(c);
                index++;
                while (index < text.length && Character.isDigit(text[index])) {
                    str.append(text[index]);
                    index++;
                }
                if (str.toString().equals("-")) return Triplet.with(LexicalType.error, null, null);
                return Triplet.with(LexicalType.digit, Long.valueOf(str.toString()), null);
            } else if (c == '!') {
                index++;
                return Triplet.with(LexicalType.negate, null, c);
            } else if (c == '|' || c == '&' || c == ',' || c == '?' || c == ':') {
                index++;
                return Triplet.with(LexicalType.character, null, c);
            } else if (c == '(' || c == ')') {
                index++;
                return Triplet.with(LexicalType.brackets, null, c);
            } else if (Character.isLetter(c)) {
                StringBuilder str = new StringBuilder();
                str.append(c);
                index++;
                while (index < text.length && Character.isLetterOrDigit(text[index])) {
                    str.append(text[index]);
                    index++;
                }
                String s = str.toString().toLowerCase();
                if ("true".equals(s))
                    return Triplet.with(LexicalType.bool, 1L, null);
                else if ("false".equals(s))
                    return Triplet.with(LexicalType.bool, 0L, null);
                else
                    return Triplet.with(LexicalType.error, null, null);
            } else
                return Triplet.with(LexicalType.error, null, null);
        } else return Triplet.with(LexicalType.end, null, null);
    }

    private boolean calc(Pair<Integer, Boolean> end, LongPredicate judge) {
        if (result.size() != action.size() + 1) return false;
        int n = result.size() - end.getValue0();
        if (n <= 1) return true;
        LongBool[] b = new LongBool[n];
        char[] c = new char[n - 1];
        for (int i = n - 1; i >= 0; i--)
            b[i] = result.removeLast();
        for (int i = n - 2; i >= 0; i--) {
            c[i] = action.removeLast();
        }
        Boolean res = calc1(b, c, 0, n - 1, judge);
        if (res == null) return false;
        result.addLast(new LongBool(res).setNegate(end.getValue1()));
        return true;
    }

    private Boolean calc1(LongBool[] b, char[] c, int start, int end, LongPredicate judge) {
        boolean comma = (c[start] == ',');
        if (comma) {
            Long number = b[start].getL(), true_number = 0L, all_number = 0L;
            int tmp_start = start + 1;
            if (number == null) return null;
            for (int i = start + 1; i < end; i++) {
                if (c[i] == ',') {
                    Boolean calc2 = calc2(b, c, tmp_start, i, judge);
                    if (calc2 == null) return null;
                    all_number++;
                    if (calc2) true_number++;
                    tmp_start = i + 1;
                }
            }
            Boolean calc2 = calc2(b, c, tmp_start, end, judge);
            if (calc2 == null) return null;
            all_number++;
            if (calc2) true_number++;
            if (number <= 0) number = all_number + number;
            return true_number >= number;
        } else {
            for (int i = start; i < end; i++) {
                if (c[i] == ',') return null;
            }
            return calc2(b, c, start, end, judge);
        }
    }

    private Boolean calc2(LongBool[] b, char[] c, int start, int end, LongPredicate judge) {
        if (start > end) return null;
        else if (start == end) return b[start].get(judge);
        else {
            Character before = null;
            int tmp_start = start;
            LinkedList<Boolean> stack = new LinkedList<>();
            for (int i = start; i < end + 1; i++) {
                if (i == end || c[i] == '?' || c[i] == ':') {
                    Boolean calc3 = calc3(b, c, tmp_start, i, judge);
                    if (calc3 == null) return null;
                    if (before == null) {
                        stack.addLast(calc3);
                    } else if (before == '?') {
                        stack.addLast(calc3);
                    } else if (before == ':') {
                        boolean first, second, result;
                        if (!stack.isEmpty()) {
                            second = stack.removeLast();
                        } else return null;
                        if (!stack.isEmpty()) {
                            first = stack.removeLast();
                        } else return null;
                        result = first ? second : calc3;
                        stack.addLast(result);
                    } else return null;
                    if (i < end) {
                        before = c[i];
                        tmp_start = i + 1;
                    }
                }
            }
            if (stack.size() != 1) return null;
            return stack.removeLast();
        }
    }

    private Boolean calc3(LongBool[] b, char[] c, int start, int end, LongPredicate judge) {
        if (start > end) return null;
        boolean res = b[start].get(judge);
        for (int i = start; i < end; i++) {
            if (c[i] == '&')
                res = res && b[i + 1].get(judge);
            else if (c[i] == '|')
                res = res || b[i + 1].get(judge);
            else
                return null;
        }
        return res;
    }

    public JudgeText(String text) {
        if (text == null) text = "";
        this.text = text.replaceAll("\\s*", "").toCharArray();
    }

    private static class LongBool {
        public boolean get(LongPredicate judge) {
            boolean res;
            if (this.getB() != null) res = this.getB();
            else res = judge.test(this.getL());
            if (this.isNegate()) res = !res;
            return res;
        }

        LongBool(Long l) {
            this.l = l;
            this.b = null;
        }

        LongBool(boolean b) {
            this.l = null;
            this.b = b;
        }

        Long getL() {
            return l;
        }

        private Boolean getB() {
            return b;
        }

        private boolean isNegate() {
            return negate;
        }

        LongBool setNegate(boolean negate) {
            this.negate = negate;
            return this;
        }

        private final Long l;
        private final Boolean b;
        private boolean negate = false;
    }

    private enum LexicalType {
        digit,
        character,
        brackets,
        bool,
        end,
        error,
        negate,
    }

    private final LinkedList<LongBool> result = new LinkedList<>();
    private final LinkedList<Pair<Integer, Boolean>> brackets = new LinkedList<>();
    private final LinkedList<Character> action = new LinkedList<>();

    private int index;

    private final char[] text;
}
