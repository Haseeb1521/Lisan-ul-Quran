#!/usr/bin/env python3
"""Downloads Quran text, word-by-word meanings, translations and tafsir from the
quran.com public API and writes compact JSON files into the Android assets folder."""
import json
import os
import re
import sys
import time
import urllib.parse
import urllib.request

BASE = "https://api.quran.com/api/v4"
OUT = sys.argv[1] if len(sys.argv) > 1 else "android/app/src/main/assets"
EXPECTED_AYAHS = 6236


def get(path, params=None):
    url = BASE + path
    if params:
        url += "?" + urllib.parse.urlencode(params)
    last = None
    for attempt in range(8):
        try:
            req = urllib.request.Request(
                url, headers={"Accept": "application/json", "User-Agent": "lisan-ul-quran-build"}
            )
            with urllib.request.urlopen(req, timeout=90) as r:
                data = json.load(r)
            time.sleep(0.1)
            return data
        except Exception as e:  # noqa: BLE001
            last = e
            print(f"retry {attempt + 1} for {url}: {e}", file=sys.stderr)
            time.sleep(2 * (attempt + 1))
    raise SystemExit(f"FAILED to fetch {url}: {last}")


def clean(text):
    if not text:
        return ""
    text = re.sub(r"<sup[^>]*>.*?</sup>", "", text, flags=re.S)
    text = re.sub(r"<[^>]+>", "", text)
    return re.sub(r"\s+", " ", text).strip()


def pick_translation(translations, language, prefer):
    pool = [t for t in translations if t.get("language_name", "").lower() == language]
    for key in prefer:
        for t in pool:
            blob = (t.get("name", "") + " " + t.get("author_name", "")).lower()
            if key in blob:
                return t
    return pool[0] if pool else None


def fetch_verses(chapter_id, language, translation_ids):
    verses = []
    page = 1
    while True:
        params = {
            "language": language,
            "words": "true",
            "word_fields": "text_uthmani",
            "fields": "text_uthmani",
            "per_page": 50,
            "page": page,
        }
        if translation_ids:
            params["translations"] = ",".join(str(i) for i in translation_ids)
        data = get(f"/verses/by_chapter/{chapter_id}", params)
        verses.extend(data["verses"])
        nxt = data.get("pagination", {}).get("next_page")
        if not nxt:
            break
        page = nxt
    return verses


def real_words(verse):
    return [w for w in verse.get("words", []) if w.get("char_type_name") == "word"]


def word_text(w):
    return (w.get("text_uthmani") or w.get("text") or "").strip()


def main():
    os.makedirs(OUT, exist_ok=True)

    chapters_raw = get("/chapters", {"language": "en"})["chapters"]
    chapters = [
        {
            "id": c["id"],
            "ar": c["name_arabic"],
            "en": c["name_simple"],
            "tr": c.get("translated_name", {}).get("name", ""),
            "n": c["verses_count"],
        }
        for c in chapters_raw
    ]
    print(f"chapters: {len(chapters)}")

    trs = get("/resources/translations")["translations"]
    urdu = pick_translation(trs, "urdu", ["jalandh", "maududi", "junagarhi"])
    english = pick_translation(trs, "english", ["saheeh", "clear quran", "khattab"])
    print("urdu translation:", urdu and (urdu["id"], urdu.get("name"), urdu.get("author_name")))
    print("english translation:", english and (english["id"], english.get("name"), english.get("author_name")))
    ids = [t["id"] for t in (urdu, english) if t]

    ayahs = []
    total_words = 0
    ur_words = 0
    for ch in chapters:
        cid = ch["id"]
        en_verses = fetch_verses(cid, "en", ids)
        ur_verses = fetch_verses(cid, "ur", [])
        ur_by_num = {v["verse_number"]: v for v in ur_verses}
        for v in en_verses:
            num = v["verse_number"]
            ur_v = ur_by_num.get(num)
            ur_ws = real_words(ur_v) if ur_v else []
            words = []
            for i, w in enumerate(real_words(v)):
                tr = clean((w.get("transliteration") or {}).get("text", ""))
                en = clean((w.get("translation") or {}).get("text", ""))
                ur = ""
                if i < len(ur_ws):
                    ur = clean((ur_ws[i].get("translation") or {}).get("text", ""))
                words.append([word_text(w), tr, en, ur])
                total_words += 1
                if ur:
                    ur_words += 1
            tmap = {t.get("resource_id"): clean(t.get("text", "")) for t in v.get("translations", [])}
            ayahs.append(
                {
                    "s": cid,
                    "a": num,
                    "ar": (v.get("text_uthmani") or "").strip(),
                    "ur": tmap.get(urdu["id"], "") if urdu else "",
                    "en": tmap.get(english["id"], "") if english else "",
                    "w": words,
                }
            )
        print(f"surah {cid}/{len(chapters)} done ({len(ayahs)} ayahs so far)")

    if len(ayahs) != EXPECTED_AYAHS:
        raise SystemExit(f"Expected {EXPECTED_AYAHS} ayahs but got {len(ayahs)}")
    print(f"ayahs: {len(ayahs)}, words: {total_words}, words with urdu meaning: {ur_words}")

    with open(os.path.join(OUT, "quran.json"), "w", encoding="utf-8") as f:
        json.dump({"chapters": chapters, "ayahs": ayahs}, f, ensure_ascii=False, separators=(",", ":"))

    for language, fname, prefs in (
        ("urdu", "tafsir.json", ["bayan"]),
        ("english", "tafsir_en.json", ["ibn kathir (abridged)", "kathir", "ma'arif"]),
    ):
        try:
            fetch_tafsir(chapters, language, fname, prefs)
        except (SystemExit, Exception) as e:  # noqa: BLE001
            print("TAFSIR SKIPPED", language, e, file=sys.stderr)
            write_tafsir(fname, "", [])


def write_tafsir(fname, source, items):
    with open(os.path.join(OUT, fname), "w", encoding="utf-8") as f:
        json.dump({"source": source, "items": items}, f, ensure_ascii=False, separators=(",", ":"))
    print(f"{fname}: tafsir items: {len(items)} source: {source}")


def tafsir_text(html):
    html = re.sub(r"</p>|<br\s*/?>|</div>|</h\d>", "\n", html or "")
    html = re.sub(r"<[^>]+>", "", html)
    lines = [re.sub(r"[ \t]+", " ", ln).strip() for ln in html.split("\n")]
    return "\n".join(ln for ln in lines if ln)


def fetch_tafsir(chapters, language, fname, prefs):
    lst = get("/resources/tafsirs")["tafsirs"]
    pool = [t for t in lst if t.get("language_name", "").lower() == language]
    chosen = None
    for key in prefs:
        for t in pool:
            if key in (t.get("name", "") + " " + t.get("author_name", "")).lower():
                chosen = t
                break
        if chosen:
            break
    if not chosen and pool:
        chosen = pool[0]
    if not chosen:
        raise SystemExit("no suitable tafsir found for " + language)
    print("chosen tafsir:", language, chosen["id"], chosen.get("name"))
    items = []
    for ch in chapters:
        page = 1
        while True:
            d = get(f"/tafsirs/{chosen['id']}/by_chapter/{ch['id']}", {"per_page": 50, "page": page})
            for t in d.get("tafsirs", []):
                key = t.get("verse_key", "")
                if ":" not in key:
                    continue
                s_, a_ = key.split(":")
                text = tafsir_text(t.get("text", ""))
                if text:
                    items.append({"s": int(s_), "a": int(a_), "t": text})
            nxt = d.get("pagination", {}).get("next_page")
            if not nxt:
                break
            page = nxt
    write_tafsir(fname, chosen.get("name", ""), items)


if __name__ == "__main__":
    main()
