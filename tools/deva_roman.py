"""Devanagari (Hindi/Urdu-in-Devanagari) -> Roman (Hinglish / Roman Urdu) transliteration.

Devanagari writes its vowels, so the result is far more reliable than transliterating
Urdu script. The conventions follow common Roman Urdu spelling (ee, oo, kh, gh, q, z)."""
import re
import unicodedata

N = lambda s: unicodedata.normalize("NFD", s)  # noqa: E731

CONS = {
    "क": "k", "ख": "kh", "ग": "g", "घ": "gh", "ङ": "n",
    "च": "ch", "छ": "chh", "ज": "j", "झ": "jh", "ञ": "n",
    "ट": "t", "ठ": "th", "ड": "d", "ढ": "dh", "ण": "n",
    "त": "t", "थ": "th", "द": "d", "ध": "dh", "न": "n",
    "प": "p", "फ": "f", "ब": "b", "भ": "bh", "म": "m",
    "य": "y", "र": "r", "ल": "l", "व": "w", "श": "sh", "ष": "sh", "स": "s", "ह": "h",
}
NUKTA = {"क": "q", "ख": "kh", "ग": "gh", "ज": "z", "ड": "r", "ढ": "rh", "फ": "f", "य": "y"}
INDEP = {
    "अ": "a", "आ": "aa", "इ": "i", "ई": "ee", "उ": "u", "ऊ": "oo", "ऋ": "ri",
    "ए": "e", "ऐ": "ai", "ओ": "o", "औ": "au",
}
MATRA = {
    "ा": "a", "ि": "i", "ी": "ee", "ु": "u", "ू": "oo", "ृ": "ri",
    "े": "e", "ै": "ai", "ो": "o", "ौ": "au", "ॉ": "o", "ॅ": "e",
}
VIRAMA = "्"
NUKTA_CH = "़"
NASAL = {"ं": "n", "ँ": "n", "ः": "h"}

OVERRIDES = {N(k): v for k, v in {
    "में": "mein", "मैं": "main", "नहीं": "nahin", "है": "hai", "हैं": "hain", "हूँ": "hoon",
    "और": "aur", "उन्होंने": "unhone", "लिए": "liye", "हम": "hum", "तुम": "tum", "तू": "tu",
    "वह": "woh", "वो": "woh", "यह": "yeh", "ये": "ye", "क्या": "kya", "कि": "ki",
    "अल्लाह": "Allah", "ख़ुदा": "khuda", "रब": "rab", "ईमान": "eman", "क़ुरआन": "Quran", "कुरआन": "Quran",
    "मुहम्मद": "Muhammad", "मूसा": "Musa", "ईसा": "Isa", "इब्राहीम": "Ibrahim", "फ़िरऔन": "Firaun",
}.items()}

RUN = re.compile("[ऀ-ॣ०-ॿ]+")


def _word(word):
    w = N(word)
    if w in OVERRIDES:
        return OVERRIDES[w]
    w = w.replace(N("ज्ञ"), N("ग्य"))
    units = []  # [cons, vowel]  vowel None => inherent 'a'
    cluster = ""
    i = 0
    n = len(w)
    while i < n:
        ch = w[i]
        if ch in CONS:
            base = CONS[ch]
            j = i + 1
            if j < n and w[j] == NUKTA_CH:
                base = NUKTA.get(ch, base)
                j += 1
            if j < n and w[j] == VIRAMA:
                cluster += base
                i = j + 1
                continue
            if j < n and w[j] in MATRA:
                units.append([cluster + base, MATRA[w[j]]])
                cluster = ""
                i = j + 1
                continue
            units.append([cluster + base, None])
            cluster = ""
            i = j
            continue
        if ch in INDEP:
            if cluster:
                units.append([cluster, ""])
                cluster = ""
            units.append(["", INDEP[ch]])
            i += 1
            continue
        if ch in NASAL:
            if cluster:
                units.append([cluster, ""])
                cluster = ""
            if units:
                cur = units[-1][1] if units[-1][1] is not None else "a"
                if cur == "e" and ch in "ंँ":
                    cur = "ei"
                units[-1][1] = cur + NASAL[ch]
            i += 1
            continue
        if ch == NUKTA_CH or ch == VIRAMA:
            i += 1
            continue
        i += 1
    if cluster:
        units.append([cluster, ""])

    n_units = len(units)
    keep = [True] * n_units
    for idx, (_, v) in enumerate(units):
        if v is not None:
            continue
        if idx == n_units - 1 and n_units > 1:
            keep[idx] = False
        elif 0 < idx < n_units - 1:
            prev_has = units[idx - 1][1] is None and keep[idx - 1] or (units[idx - 1][1] not in (None, ""))
            nxt = units[idx + 1]
            next_has = (nxt[1] not in (None, "")) or (nxt[1] is None and idx + 1 < n_units - 1)
            if prev_has and next_has:
                keep[idx] = False
    out = []
    for idx, (c, v) in enumerate(units):
        if v is None:
            out.append(c + ("a" if keep[idx] else ""))
        else:
            out.append(c + v)
    return "".join(out)


def to_roman(text):
    if not text:
        return ""
    text = text.replace("।", ".").replace("॥", ".")
    res = RUN.sub(lambda m: _word(m.group(0)), text)
    return re.sub(r"\s+", " ", res).strip()


if __name__ == "__main__":
    samples = [
        "सब तारीफ़", "अल्लाह के लिए है", "जो रब है", "तमाम जहानों का",
        "और जब", "फ़रमाया", "आपके रब ने", "फ़रिश्तों से", "बेशक मैं", "बनाने वाला हूँ", "ज़मीन में",
        "एक ख़लीफ़ा", "उन्होंने कहा", "क्या तू बनाएगा", "फ़साद करेगा", "और वो बहाएगा", "ख़ून",
        "हम तस्बीह करते हैं", "साथ तेरी तारीफ़ के", "और हम पाकीज़गी बयान करते हैं", "मैं जानता हूँ",
        "नहीं", "तुम जानते", "(30)", "in",
    ]
    for s in samples:
        print(f"{s}  ->  {to_roman(s)}")
