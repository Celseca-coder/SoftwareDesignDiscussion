import Typo from 'typo-js';
const dictionary = new Typo('en_US');

export function checkSpell(word: string): string[] | null {
    if (dictionary.check(word)) return null;
    return dictionary.suggest(word);
}
