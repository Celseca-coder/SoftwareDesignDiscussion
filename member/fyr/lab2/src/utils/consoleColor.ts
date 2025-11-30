import chalk from 'chalk';

const _console_warn = console.warn;
console.warn = function (...args: any[]) {
    args = args.map(arg => chalk.yellow(arg));
    _console_warn.apply(console, args);
};

const _console_error = console.error;
console.error = function (...args: any[]) {
    args = args.map(arg => chalk.red(arg));
    _console_error.apply(console, args);
};
