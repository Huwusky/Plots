package com.edawg878.bukkit.commands

import com.edawg878.common.Command.Bukkit.{BukkitOptionParser, BukkitCommand}
import com.edawg878.common.Command.IntOps._
import com.edawg878.common.Readers.PlayerDataReader
import com.edawg878.common.{PlayerData, PlayerRepository}
import org.bukkit.command.CommandSender

import scala.concurrent.Future

/**
 * @author EDawg878 <EDawg878@gmail.com>
 */
object Credit {

  case class Config(fn: IntOp, data: Future[PlayerData], credits: Int)

  class CreditCommand(val db: PlayerRepository) extends BukkitCommand[Config] with PlayerDataReader {

    override val default: Config = Config(fn = Show, data = null, credits = 1)

    override val parser = new BukkitOptionParser[Config]("/credit") {
      arg[IntOp]("<operation>") required() action { (x, c) =>
        c.copy(fn = x)
      } text "operations: +, -, set, show"
      arg[Future[PlayerData]]("<player>") required() action { (x, c) =>
        c.copy(data = x)
      } text "player to modify"
      arg[Int]("<amount>") optional() action { (x, c) =>
        c.copy(credits = x)
      } text "number of credits to add/subtract/set"
    }

    override def handle(sender: CommandSender, c: Config): Unit = {
      onComplete(sender, c.data) { data =>
        c.fn match {
          case Add | Subtract | Set =>
            val updated = data.copy(voteCredits = c.fn(data.voteCredits, c.credits).max(0))
            sender.sendMessage(updated.displayCredits)
            db.save(updated)
          case Show => sender.sendMessage(data.displayCredits)
        }
      }
    }

  }

}