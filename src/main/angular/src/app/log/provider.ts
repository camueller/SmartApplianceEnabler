import {Options, Logger} from './logger';
import {Level} from './level';

/**
 * Custom Providers if the user wants to avoid some configuration for common scenarios.
 * @type {Provider|Logger[]}
 *
 * Created by Langley on 8/24/2016.
 * https://github.com/code-chunks/angular2-logger
 */
export const OFF_LOGGER_PROVIDERS: any[] = [ { provide: Options, useValue: { level: Level.OFF } }, Logger ];
export const ERROR_LOGGER_PROVIDERS: any[] = [ { provide: Options, useValue: { level: Level.ERROR } }, Logger ];
export const WARN_LOGGER_PROVIDERS: any[] = [ { provide: Options, useValue: { level: Level.WARN } }, Logger ];
export const INFO_LOGGER_PROVIDERS: any[] = [ { provide: Options, useValue: { level: Level.INFO } }, Logger ];
export const DEBUG_LOGGER_PROVIDERS: any[] = [ { provide: Options, useValue: { level: Level.DEBUG } }, Logger ];
export const LOG_LOGGER_PROVIDERS: any[] = [ { provide: Options, useValue: { level: Level.LOG } }, Logger ];
