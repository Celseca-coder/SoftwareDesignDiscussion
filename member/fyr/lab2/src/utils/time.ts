
export function nowTimestamp(): string {
    const d = new Date();

    const YYYY = d.getFullYear().toString();
    const MM = (d.getMonth() + 1).toString().padStart(2, '0');
    const DD = d.getDate().toString().padStart(2, '0');
    const hh = d.getHours().toString().padStart(2, '0');
    const mm = d.getMinutes().toString().padStart(2, '0');
    const ss = d.getSeconds().toString().padStart(2, '0');

    return `${YYYY}${MM}${DD} ${hh}:${mm}:${ss}`;
}
