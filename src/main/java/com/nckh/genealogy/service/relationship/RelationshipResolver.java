package com.nckh.genealogy.service.relationship;

import org.springframework.stereotype.Component;

@Component
public class RelationshipResolver {

    // gender: 1=MALE, 2=FEMALE
    // generationDiff: B so với A (dương = B cao hơn thế hệ)
    // stepsUp: số bước từ A lên LCA
    // stepsDown: số bước từ LCA xuống B
    // throughFather: LCA đến A qua cha hay mẹ (ảnh hưởng nội/ngoại)
    // throughFatherToB: LCA đến B qua cha hay mẹ
    // olderThanA: B có lớn hơn A không (dùng cho anh/chị/em)

    public String resolve(
            short genderB,
            int stepsUp,      // A lên LCA
            int stepsDown,    // LCA xuống B
            boolean throughFatherUp,   // từ A lên LCA qua cha?
            boolean throughFatherDown, // từ LCA xuống B qua cha?
            boolean olderThanA,
            boolean isSpouseRelation,
            short genderA
    ) {
        // ── Vợ chồng trực tiếp ───────────────────────────────────────────────
        if (isSpouseRelation && stepsUp == 0 && stepsDown == 0) {
            return genderB == 1 ? "Chồng" : "Vợ";
        }

        // ── Trực hệ lên (A là con cháu của B) ───────────────────────────────
        if (stepsUp > 0 && stepsDown == 0) {
            return resolveAncestor(genderB, stepsUp, throughFatherUp);
        }

        // ── Trực hệ xuống (B là con cháu của A) ─────────────────────────────
        if (stepsUp == 0 && stepsDown > 0) {
            return resolveDescendant(genderB, stepsDown, throughFatherDown);
        }

        // ── Quan hệ ngang (qua LCA) ──────────────────────────────────────────
        if (stepsUp > 0 && stepsDown > 0) {
            return resolveCollateral(
                    genderB, genderA, stepsUp, stepsDown,
                    throughFatherUp, throughFatherDown, olderThanA
            );
        }

        return "Họ hàng";
    }

    // ── Tổ tiên ──────────────────────────────────────────────────────────────
    private String resolveAncestor(short gender, int steps, boolean throughFather) {
        boolean male = gender == 1;
        return switch (steps) {
            case 1 -> male ? "Cha" : "Mẹ";
            case 2 -> {
                if (throughFather) yield male ? "Ông nội" : "Bà nội";
                else yield male ? "Ông ngoại" : "Bà ngoại";
            }
            case 3 -> {
                if (throughFather) yield male ? "Cụ nội" : "Cụ nội";
                else yield male ? "Cụ ngoại" : "Cụ ngoại";
            }
            case 4 -> "Kỵ";
            case 5 -> "Sơ";
            default -> "Tổ tiên";
        };
    }

    // ── Hậu duệ ──────────────────────────────────────────────────────────────
    private String resolveDescendant(short gender, int steps, boolean throughSon) {
        boolean male = gender == 1;
        return switch (steps) {
            case 1 -> male ? "Con trai" : "Con gái";
            case 2 -> {
                if (throughSon) yield male ? "Cháu nội trai" : "Cháu nội gái";
                else yield male ? "Cháu ngoại trai" : "Cháu ngoại gái";
            }
            case 3 -> male ? "Chắt trai" : "Chắt gái";
            case 4 -> male ? "Chút trai" : "Chút gái";
            case 5 -> male ? "Chít trai" : "Chít gái";
            default -> male ? "Hậu duệ trai" : "Hậu duệ gái";
        };
    }

    // ── Quan hệ ngang ────────────────────────────────────────────────────────
    private String resolveCollateral(
            short genderB, short genderA,
            int stepsUp, int stepsDown,
            boolean throughFatherUp, boolean throughFatherDown,
            boolean olderThanA
    ) {
        boolean male = genderB == 1;
        boolean aIsMale = genderA == 1;

        // Anh chị em ruột (cùng cha mẹ)
        if (stepsUp == 1 && stepsDown == 1) {
            if (male) return olderThanA ? "Anh" : "Em trai";
            else return olderThanA ? "Chị" : "Em gái";
        }

        // Bác, Chú, Cô, Dì, Cậu (cha/mẹ của A là anh/chị/em của B)
        if (stepsUp == 2 && stepsDown == 1) {
            if (throughFatherUp) {
                // Bên cha
                if (male) return olderThanA ? "Bác trai" : "Chú";
                else return olderThanA ? "Bác gái" : "Cô";
            } else {
                // Bên mẹ
                if (male) return "Cậu";
                else return olderThanA ? "Bác gái" : "Dì";
            }
        }

        // Cháu (con của anh/chị/em)
        if (stepsUp == 1 && stepsDown == 2) {
            if (throughFatherDown) {
                // B là con của anh/em trai A
                return male ? "Cháu trai" : "Cháu gái";
            } else {
                return male ? "Cháu trai" : "Cháu gái";
            }
        }

        // Anh chị em họ (cùng ông bà)
        if (stepsUp == 2 && stepsDown == 2) {
            if (male) return olderThanA ? "Anh họ" : "Em họ trai";
            else return olderThanA ? "Chị họ" : "Em họ gái";
        }

        // Bác họ, Chú họ, Cô họ... (ông/bà của A là anh/chị/em của cha/mẹ của B)
        if (stepsUp == 3 && stepsDown == 2) {
            if (throughFatherUp) {
                if (male) return olderThanA ? "Bác họ trai" : "Chú họ";
                else return olderThanA ? "Bác họ gái" : "Cô họ";
            } else {
                if (male) return "Cậu họ";
                else return olderThanA ? "Bác họ gái" : "Dì họ";
            }
        }

        // Cháu họ
        if (stepsUp == 2 && stepsDown == 3) {
            return male ? "Cháu họ trai" : "Cháu họ gái";
        }

        // Chú họ xa, cô họ xa...
        if (stepsUp >= 3 && stepsDown >= 3) {
            int diff = stepsUp - stepsDown;
            if (diff > 0) {
                // B cao hơn thế hệ
                if (male) return "Chú họ xa";
                else return throughFatherUp ? "Cô họ xa" : "Dì họ xa";
            } else if (diff < 0) {
                return male ? "Cháu họ xa trai" : "Cháu họ xa gái";
            } else {
                return male ? "Anh họ xa" : "Chị họ xa";
            }
        }

        return "Họ hàng xa";
    }

    // ── Quan hệ thông gia ────────────────────────────────────────────────────
    public String resolveInLaw(
            short genderB, short genderA,
            int stepsUp, int stepsDown,
            boolean isSpouseOfDescendant, // B là vợ/chồng của con cháu A
            boolean isSpouseOfAncestor    // B là vợ/chồng của cha mẹ A
    ) {
        boolean male = genderB == 1;
        boolean aIsMale = genderA == 1;

        // Bố vợ/Mẹ vợ hoặc Bố chồng/Mẹ chồng
        if (stepsUp == 1) {
            if (aIsMale) return male ? "Bố vợ" : "Mẹ vợ";
            else return male ? "Bố chồng" : "Mẹ chồng";
        }

        // Con dâu/Con rể
        if (stepsDown == 1) {
            return male ? "Con rể" : "Con dâu";
        }

        // Anh rể/Chị dâu/Em rể/Em dâu
        if (stepsUp == 1 && stepsDown == 1) {
            if (male) return "Anh rể / Em rể";
            else return "Chị dâu / Em dâu";
        }

        return "Thông gia";
    }
    /**
     * B là vợ/chồng của người cách A [stepsDown] bước xuống
     * stepsDown=1 → con dâu/rể
     * stepsDown=2 → cháu dâu/rể
     * stepsDown=3 → chắt dâu/rể
     */
    public String resolveDaughterInLaw(short genderB, short genderA, int stepsDown, int stepsUp) {
        boolean male = genderB == 1;
        // stepsUp > 0: B là vợ/chồng của người trên A (dì ghẻ, cha dượng...)
        if (stepsUp == 1 && stepsDown == 0) {
            return male ? "Cha dượng" : "Mẹ kế";
        }
        return switch (stepsDown) {
            case 1 -> male ? "Con rể" : "Con dâu";
            case 2 -> male ? "Cháu rể" : "Cháu dâu";
            case 3 -> male ? "Chắt rể" : "Chắt dâu";
            default -> male ? "Hậu duệ rể" : "Hậu duệ dâu";
        };
    }

    /**
     * B là họ hàng của vợ/chồng A
     * A ↔ Spouse → ... → B
     * stepsDown=1 từ Spouse xuống B → B là con vợ/chồng A
     * stepsUp=1 từ Spouse lên B → B là bố/mẹ vợ/chồng A
     */
    public String resolveSpouseRelative(short genderB, short genderA, int stepsUp, int stepsDown) {
        boolean male = genderB == 1;
        boolean aIsMale = genderA == 1;

        // Bố/Mẹ vợ hoặc chồng
        if (stepsUp == 1 && stepsDown == 0) {
            if (aIsMale) return male ? "Bố vợ" : "Mẹ vợ";
            else         return male ? "Bố chồng" : "Mẹ chồng";
        }
        // Ông/Bà vợ hoặc chồng
        if (stepsUp == 2 && stepsDown == 0) {
            if (aIsMale) return male ? "Ông vợ" : "Bà vợ";
            else         return male ? "Ông chồng" : "Bà chồng";
        }
        // Cụ/Cố vợ hoặc chồng
        if (stepsUp == 3 && stepsDown == 0) {
            if (aIsMale) return male ? "Cụ vợ" : "Cụ vợ";
            else         return male ? "Cụ chồng" : "Cụ chồng";
        }
        // Kỵ vợ hoặc chồng
        if (stepsUp == 4 && stepsDown == 0) {
            if (aIsMale) return "Kỵ vợ";
            else         return "Kỵ chồng";
        }
        // Tổ tiên xa hơn
        if (stepsUp >= 5 && stepsDown == 0) {
            if (aIsMale) return "Tổ tiên vợ";
            else         return "Tổ tiên chồng";
        }

        // Anh/Chị/Em vợ hoặc chồng
        if (stepsUp == 1 && stepsDown == 1) {
            if (aIsMale) return male ? "Anh vợ / Em vợ" : "Chị vợ / Em vợ";
            else         return male ? "Anh chồng / Em chồng" : "Chị chồng / Em chồng";
        }
        // Con riêng của vợ/chồng
        if (stepsUp == 0 && stepsDown == 1) {
            return male ? "Con riêng trai" : "Con riêng gái";
        }
        // Cháu vợ/chồng
        if (stepsUp == 1 && stepsDown == 2) {
            if (aIsMale) return male ? "Cháu trai vợ" : "Cháu gái vợ";
            else         return male ? "Cháu trai chồng" : "Cháu gái chồng";
        }

        return "Họ hàng thông gia";
    }

    /**
     * Thông gia thực sự: 2 họ kết nối qua hôn nhân của con cháu
     * A → ... → X ↔ Y → ... → B
     */
    public String resolveInLaw(short genderB, short genderA,
                               int stepsUp, int stepsDown) {
        boolean male = genderB == 1;
        boolean aIsMale = genderA == 1;

        // Cùng thế hệ → thông gia (bố mẹ 2 bên)
        if (stepsUp == 0 && stepsDown == 0) {
            return "Thông gia";
        }
        // B cao hơn A → B là thông gia bề trên
        if (stepsUp > stepsDown) {
            return male ? "Thông gia bề trên" : "Thông gia bề trên";
        }
        // B thấp hơn A
        if (stepsDown > stepsUp) {
            return "Thông gia bề dưới";
        }

        return "Thông gia";
    }
}